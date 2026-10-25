package dev.slne.surf.protect.paper.region.structure

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.api.core.util.mutableLong2ObjectMapOf
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.structureSurchargeConfig
import dev.slne.surf.protect.paper.util.fixedVolume
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.World
import org.bukkit.util.BoundingBox
import kotlin.math.max
import kotlin.math.min

/**
 * The footprints of all structures with a surcharge inside the area a protection can be created in.
 *
 * Every block column stores the structure with the highest surcharge whose pieces cover it,
 * so overlapping structures are only charged once.
 */
class StructureFootprint private constructor(
    private val columns: Long2ObjectMap<String>,
    private val surcharges: Map<String, Double>
) {

    /**
     * Calculates the surcharge for all structure columns inside [region].
     *
     * @param alreadyPaid a region whose columns are already paid for (e.g. the region being expanded)
     */
    fun calculateSurcharge(
        region: ProtectedRegion,
        alreadyPaid: ProtectedRegion? = null
    ): StructureSurcharge {
        if (columns.isEmpty()) return StructureSurcharge.NONE

        val polygon = RegionPolygon(region)
        val paidPolygon = alreadyPaid?.let { RegionPolygon(it) }

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

        val chargedArea = region.fixedVolume() - (alreadyPaid?.fixedVolume() ?: 0)
        capToArea(blocksByStructure, chargedArea)

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

    companion object {
        val EMPTY = StructureFootprint(Long2ObjectMaps.emptyMap(), emptyMap())

        /**
         * Collects the footprints of all structures with a surcharge that intersect the square
         * area with the given [radius] around [centerX] and [centerZ].
         *
         * @param alreadyPaid a region whose columns are already paid for. Chunks that lie completely
         * inside it are not loaded.
         */
        suspend fun load(
            world: World,
            centerX: Int,
            centerZ: Int,
            radius: Int,
            alreadyPaid: ProtectedRegion? = null
        ): StructureFootprint {
            val surcharges = structureSurchargeConfig.pricedStructures
            if (surcharges.isEmpty()) return EMPTY

            val paidPolygon = alreadyPaid?.let { RegionPolygon(it) }

            val minX = centerX - radius
            val maxX = centerX + radius
            val minZ = centerZ - radius
            val maxZ = centerZ + radius

            val chunks = mutableObjectListOf<Pair<Int, Int>>()
            for (chunkX in (minX shr 4)..(maxX shr 4)) {
                for (chunkZ in (minZ shr 4)..(maxZ shr 4)) {
                    if (paidPolygon != null && isChunkInside(paidPolygon, chunkX, chunkZ)) continue
                    chunks.add(chunkX to chunkZ)
                }
            }

            val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE)
            val pieces = chunks.mapAsync { (chunkX, chunkZ) ->
                val chunk = world.getChunkAtAsync(chunkX, chunkZ, false).await()
                    ?: return@mapAsync emptyList()

                withContext(plugin.regionDispatcher(world, chunkX, chunkZ)) {
                    chunk.structures.flatMap { structure ->
                        val key = registry.getKey(structure.structure)?.asString()
                        if (key == null || key !in surcharges) return@flatMap emptyList()

                        structure.pieces.map { key to it.boundingBox }
                    }
                }
            }.flatten()

            if (pieces.isEmpty()) return EMPTY

            val columns = mutableLong2ObjectMapOf<String>()
            for ((key, box) in pieces.distinct()) {
                val surcharge = surcharges.getValue(key)
                forEachColumn(box, minX, maxX, minZ, maxZ) { columnKey ->
                    val current = columns.get(columnKey)
                    if (current == null || surcharges.getValue(current) < surcharge) {
                        columns.put(columnKey, key)
                    }
                }
            }

            return StructureFootprint(columns, surcharges)
        }

        /**
         * Checks if every column of the chunk lies inside [region].
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
