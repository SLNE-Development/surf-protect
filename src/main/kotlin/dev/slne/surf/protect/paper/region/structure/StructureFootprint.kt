package dev.slne.surf.protect.paper.region.structure

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.api.core.util.mutableLong2ObjectMapOf
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.api.paper.util.getXFromChunkKey
import dev.slne.surf.api.paper.util.getZFromChunkKey
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.structureSurchargeConfig
import dev.slne.surf.protect.paper.util.fixedVolume
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlinx.coroutines.future.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.math.max
import kotlin.math.min

/**
 * The footprints of all structures with a surcharge below a protection that is being created.
 *
 * Chunks are only loaded once a region covers them and are cached afterwards. Missing chunks are
 * generated, so a structure can't be skipped just because nobody has been there yet.
 *
 * Every block column stores the structure with the highest surcharge whose pieces cover it,
 * so overlapping structures are only charged once. Only pieces that reach into the height
 * range of the protection ([minY] to [maxY]) are counted.
 */
class StructureFootprint(
    private val world: World,
    private val minY: Int,
    private val maxY: Int
) {
    private val surcharges = structureSurchargeConfig.pricedStructures
    private val columns = mutableLong2ObjectMapOf<String>()
    private val loadedChunks = LongOpenHashSet()
    private val mutex = Mutex()

    /**
     * Calculates the surcharge for all structure columns inside [region].
     * Loads the chunks below the region first, if they are not cached yet.
     *
     * @param alreadyPaid a region whose columns are already paid for (e.g. the region being expanded)
     */
    suspend fun calculateSurcharge(
        region: ProtectedRegion,
        alreadyPaid: ProtectedRegion? = null
    ): StructureSurcharge {
        if (surcharges.isEmpty()) return StructureSurcharge.NONE

        val polygon = RegionPolygon(region)
        val paidPolygon = alreadyPaid?.let { RegionPolygon(it) }

        return mutex.withLock {
            loadChunks(region, paidPolygon)
            countSurcharge(region, polygon, paidPolygon)
        }
    }

    private fun countSurcharge(
        region: ProtectedRegion,
        polygon: RegionPolygon,
        paidPolygon: RegionPolygon?
    ): StructureSurcharge {
        val blocksByStructure = Object2LongOpenHashMap<String>()
        val it = Long2ObjectMaps.fastIterator(columns)
        while (it.hasNext()) {
            val entry = it.next()
            val x = columnX(entry.longKey)
            val z = columnZ(entry.longKey)
            if (!polygon.containsColumn(x, z)) continue
            if (paidPolygon != null && paidPolygon.containsColumn(x, z)) continue

            blocksByStructure.addTo(entry.value, 1)
        }

        if (blocksByStructure.isEmpty()) return StructureSurcharge.NONE

        // When expanding, the old outline may be reshaped, so the area difference is no upper bound
        if (paidPolygon == null) {
            capToArea(blocksByStructure, region.fixedVolume())
        }

        val parts = blocksByStructure.object2LongEntrySet()
            .filter { it.longValue > 0 }
            .map { entry ->
                StructureSurcharge.Part(
                    entry.key,
                    entry.longValue,
                    surcharges.getValue(entry.key)
                )
            }.sortedByDescending { it.cost }

        return StructureSurcharge(parts)
    }

    /**
     * Counting column centers can differ from the measured area by a few blocks on diagonal edges.
     * Removes the excess from the cheapest structures, so the structure blocks never exceed the area.
     */
    private fun capToArea(blocksByStructure: Object2LongOpenHashMap<String>, area: Long) {
        var excess = blocksByStructure.values.sum() - area
        if (excess <= 0) return

        for (key in blocksByStructure.keys.sortedBy { surcharges.getValue(it) }) {
            val removed = min(excess, blocksByStructure.getLong(key))
            blocksByStructure.addTo(key, -removed)
            excess -= removed
            if (excess <= 0) return
        }
    }

    /**
     * Loads all chunks below the bounding box of [region] that are not cached yet.
     * Chunks that lie completely inside the already paid region are skipped.
     */
    private suspend fun loadChunks(region: ProtectedRegion, paidPolygon: RegionPolygon?) {
        val min = region.minimumPoint
        val max = region.maximumPoint

        val missing = mutableObjectListOf<Long>()
        for (chunkX in (min.x() shr 4)..(max.x() shr 4)) {
            for (chunkZ in (min.z() shr 4)..(max.z() shr 4)) {
                val chunkKey = Chunk.getChunkKey(chunkX, chunkZ)
                if (chunkKey in loadedChunks) continue
                if (paidPolygon != null && isChunkInside(paidPolygon, chunkX, chunkZ)) continue
                missing.add(chunkKey)
            }
        }

        if (missing.isEmpty()) return

        val pieces = missing.mapAsync { chunkKey ->
            chunkLoadPermits.withPermit { loadPieces(chunkKey) }
        }

        for ((index, chunkKey) in missing.withIndex()) {
            addColumns(chunkKey, pieces[index])
            loadedChunks.add(chunkKey)
        }
    }

    /**
     * Loads (or generates) the chunk and returns all pieces of priced structures that intersect it
     * and reach into the height range of the protection.
     */
    private suspend fun loadPieces(chunkKey: Long): List<Pair<String, BoundingBox>> {
        val chunkX = getXFromChunkKey(chunkKey)
        val chunkZ = getZFromChunkKey(chunkKey)
        val chunk = world.getChunkAtAsync(chunkX, chunkZ, true).await()

        return withContext(plugin.regionDispatcher(world, chunkX, chunkZ)) {
            chunk.structures.flatMap { structure ->
                val key = registry.getKey(structure.structure)?.asString()
                if (key == null || key !in surcharges) return@flatMap emptyList()

                structure.pieces
                    .map { it.boundingBox }
                    .filter { it.maxY >= minY && it.minY <= maxY }
                    .map { key to it }
            }
        }
    }

    /**
     * Adds the columns of the pieces that lie inside the chunk. Pieces reaching into other chunks
     * are added for those chunks when they are loaded, so no column is added twice.
     */
    private fun addColumns(chunkKey: Long, pieces: List<Pair<String, BoundingBox>>) {
        val chunkMinX = getXFromChunkKey(chunkKey) shl 4
        val chunkMinZ = getZFromChunkKey(chunkKey) shl 4

        for ((key, box) in pieces) {
            val surcharge = surcharges.getValue(key)
            forEachColumn(box, chunkMinX, chunkMinX + 15, chunkMinZ, chunkMinZ + 15) { columnKey ->
                val current = columns.get(columnKey)
                if (current == null || surcharges.getValue(current) < surcharge) {
                    columns.put(columnKey, key)
                }
            }
        }
    }

    companion object {
        /** Maximum number of chunks loaded at the same time, shared by all protections being created. */
        private const val MAX_CONCURRENT_CHUNK_LOADS = 8

        private val chunkLoadPermits = Semaphore(MAX_CONCURRENT_CHUNK_LOADS)
        private val registry get() = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE)

        /**
         * Checks if every column of the chunk lies inside [polygon].
         * Checking the chunk border is enough, as regions have no holes.
         */
        private fun isChunkInside(polygon: RegionPolygon, chunkX: Int, chunkZ: Int): Boolean {
            val minX = chunkX shl 4
            val minZ = chunkZ shl 4
            val maxX = minX + 15
            val maxZ = minZ + 15

            for (i in 0..15) {
                if (!polygon.containsColumn(minX + i, minZ)) return false
                if (!polygon.containsColumn(minX + i, maxZ)) return false
                if (!polygon.containsColumn(minX, minZ + i)) return false
                if (!polygon.containsColumn(maxX, minZ + i)) return false
            }

            return true
        }

        /**
         * Iterates all block columns of [box] clipped to the given bounds.
         * Structure bounding boxes are inclusive on both ends.
         */
        private inline fun forEachColumn(
            box: BoundingBox,
            minX: Int, maxX: Int,
            minZ: Int, maxZ: Int,
            action: (Long) -> Unit
        ) {
            val fromX = max(box.minX.toInt(), minX)
            val toX = min(box.maxX.toInt(), maxX)
            val fromZ = max(box.minZ.toInt(), minZ)
            val toZ = min(box.maxZ.toInt(), maxZ)

            for (x in fromX..toX) {
                for (z in fromZ..toZ) {
                    action(columnKey(x, z))
                }
            }
        }

        private fun columnKey(x: Int, z: Int): Long = (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)
        private fun columnX(key: Long): Int = (key shr 32).toInt()
        private fun columnZ(key: Long): Int = key.toInt()

    }
}
