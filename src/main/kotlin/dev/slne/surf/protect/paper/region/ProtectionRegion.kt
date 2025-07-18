@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.region

import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.dialogs.ProtectionCreationDialogs
import dev.slne.surf.protect.paper.math.Mth
import dev.slne.surf.protect.paper.message.Messages
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.EditableProtectionFlags
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionCreationState
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.region.transaction.ProtectionBuyData
import dev.slne.surf.protect.paper.region.visual.Marker
import dev.slne.surf.protect.paper.region.visual.QuickHull
import dev.slne.surf.protect.paper.region.visual.Trail
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.*
import dev.slne.surf.surfapi.bukkit.api.util.getHighestBlockYAtBlockCoordinates
import dev.slne.surf.surfapi.bukkit.api.util.getXFromChunkKey
import dev.slne.surf.surfapi.bukkit.api.util.getZFromChunkKey
import dev.slne.surf.surfapi.core.api.util.*
import dev.slne.transaction.api.transaction.result.TransactionAddResult
import io.papermc.paper.math.BlockPosition
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.min

class ProtectionRegion(
    val protectionUser: ProtectionUser,
    val player: Player,
    playerInventoryContent: Array<ItemStack?>,
    val expandingProtection: ProtectedRegion? = null
) {
    val startLocation = player.location
    val startingInventoryContent = playerInventoryContent
    var worldBorderSize = config.protection.maxDistanceFromStart

    private val markers = mutableObjectListOf<Marker>(config.markers.amount)
    private val trails = mutableObjectSetOf<Trail>(config.markers.amount)
    private var hull = ObjectLinkedOpenHashSet<Marker>()

    private val tmpBuffer = mutableObjectListOf<Marker>()
    private var tempRegion: TempProtectionRegion? = null

    private val isProcessingTransaction = AtomicBoolean(false)

    val markerCountLeft: Int get() = maxMarkerCount - currentMarkerCount
    val maxMarkerCount: Int
        get() = config.markers.amount + (expandingProtection?.points?.size ?: 0)
    val currentMarkerCount: Int get() = markers.size

    suspend fun setCornerMarkers() {
        val region = expandingProtection ?: return
        val world = player.world
        val points = region.points

        val byChunk = mutableLong2ObjectMapOf<ObjectList<BlockVector2>>(points.size / 4 + 1)
        for (point in points) {
            val key = Chunk.getChunkKey(point.x() shr 4, point.z() shr 4)
            val list = byChunk.computeIfAbsent(key) { mutableObjectListOf() }
            list.add(point)
        }

        val snapshots = coroutineScope {
            byChunk.keys.mapAsync { key ->
                key to world.getChunkAtAsync(getXFromChunkKey(key), getZFromChunkKey(key))
                    .await()
                    .getChunkSnapshot(true, false, false, false)
            }.toMap(mutableLong2ObjectMapOf<ChunkSnapshot>(byChunk.size))
        }

        val it = byChunk.long2ObjectEntrySet().fastIterator()
        while (it.hasNext()) {
            val entry = it.next()
            val key = entry.longKey
            val pointsInChunk = entry.value
            val snapshot = snapshots[key] ?: error("ChunkSnapshot for key $key not found")
            for (point in pointsInChunk) {
                val x = point.x()
                val z = point.z()
                val y =
                    min(snapshot.getHighestBlockYAtBlockCoordinates(x, z) + 1, world.maxHeight - 1)
                val pos = point.toBlockPosition(y)
                val data = snapshot.getBlockDataAt(pos.blockX(), pos.blockY(), pos.blockZ())
                createMarker(pos, data, isExpanding = true)
            }
        }
    }

    fun createMarker(pos: BlockPosition, previousData: BlockData, isExpanding: Boolean): Marker? {
        val candidate = Marker(WeakReference(player.world), this, pos, previousData)
        if (!updateHullPreview(candidate)) return null

        // Check overlap state when not expanding
        if (!isExpanding && offerAccepting() == RegionCreationState.OVERLAPPING) {
            handleTrails()
            restoreHull()
            return null
        }

        markers.add(candidate)
        plugin.launch { candidate.place() }
        handleTrails()

        return candidate
    }

    private fun updateHullPreview(candidate: Marker): Boolean {
        tmpBuffer.clear()
        tmpBuffer.addAll(markers)
        tmpBuffer.add(candidate)
        val preview = QuickHull.compute(tmpBuffer)
        return if (preview.contains(candidate)) {
            hull.clear()
            hull.addAll(preview)
            true
        } else false
    }

    private fun restoreHull() {
        if (markers.size < 3) {
            hull.clear()
            hull.addAll(markers)
            return
        }
        hull.clear()
        hull.addAll(QuickHull.compute(markers))
    }

    /**
     * Accepts the protection
     *
     * @return the [RegionCreationState]
     */
    private fun offerAccepting(): RegionCreationState {
        if (hull.size < config.markers.minAmount) {
            protectionUser.sendMessage(Messages.Protecting.moreMarkers(hull.size))
            tempRegion = null
            return RegionCreationState.MORE_MARKERS_NEEDED
        }

        val vectors = mutableObjectListOf<BlockVector2>()
        for (marker in hull) {
            vectors.add(marker.toBlockVector2())
        }

        val world = player.world
        val manager = world.getRegionManager()
        val region: ProtectedRegion

        if (expandingProtection != null) {
            region = ProtectedPolygonalRegion(
                expandingProtection.id,
                vectors,
                world.minHeight,
                world.maxHeight - 1
            )
            region.copyFrom(expandingProtection)
        } else {
            val name = player.name + "-" + RandomStringUtils.secureStrong()
                .nextAlphabetic(ProtectionSettings.RANDOM_NAME_LENGTH)
                .uppercase()

            region = ProtectedPolygonalRegion(
                name,
                vectors,
                world.minHeight,
                world.maxHeight - 1
            )
            region.owners.addPlayer(protectionUser.localPlayer)

            for (flagsMap in EditableProtectionFlags.entries) {
                region.setFlag(
                    flagsMap.flag,
                    flagsMap.initialState
                )
            }

            region.setFlag(
                Flags.NONPLAYER_PROTECTION_DOMAINS,
                objectSetOf(player.uniqueId.toString())
            )
        }

        // Get the center of the region and set the teleport location
        val center = region.fastCenter().toVector3()
        val centerLoc = com.sk89q.worldedit.util.Location(
            world.toWorldEdit(),
            center.x(),
            center.y(),
            center.z()
        )

        region.setFlag(Flags.TELE_LOC, centerLoc)

        // Set SURF_PROTECT_FLAG if it does not exist already
        RegionInfo(region)

        // Set SURF_PROTECTION flag to ALLOW
        region.setFlag(ProtectionFlagsRegistry.SURF_PROTECTION, StateFlag.State.ALLOW)


        val tmpRegion = TempProtectionRegion(world, region, manager).also { tempRegion = it }
        val tmpVolume = tmpRegion.volume
        if (expandingProtection != null) {
            tmpRegion.effectiveVolume = tmpVolume - expandingProtection.fixedVolume()
        }

        return when {
            tmpRegion.overlaps(expandingProtection) -> RegionCreationState.OVERLAPPING.also {
                protectionUser.sendMessage(Messages.Protecting.overlappingRegions)
            }

            tmpVolume <= config.area.minBlocks -> RegionCreationState.TOO_SMALL.also {
                protectionUser.sendMessage(Messages.Protecting.areaTooSmall)
            }

            tmpVolume > config.area.maxBlocks -> RegionCreationState.TOO_LARGE.also {
                protectionUser.sendMessage(Messages.Protecting.areaTooBig)
            }

            else -> {
                val currency = config.currency
                val (effectiveCost, pricePerBlock, spawnDistance) = Mth.calculateEffectiveCost(
                    centerLoc,
                    tmpRegion
                )

                if (effectiveCost <= 0) {
                    protectionUser.sendMessage(Messages.Protecting.areaTooSmall)
                    RegionCreationState.TOO_SMALL
                } else {
                    protectionUser.sendMessage(
                        Messages.Protecting.offer(
                            tmpVolume,
                            effectiveCost,
                            currency.currency,
                            pricePerBlock,
                            spawnDistance
                        )
                    )
                    RegionCreationState.SUCCESS
                }
            }
        }
    }

    /**
     * Handles removal of marker
     *
     * @param marker the marker
     */
    suspend fun removeMarker(marker: Marker) {
        marker.restorePreviousData()
        markers.remove(marker)
        restoreHull()
        handleTrails()
        offerAccepting()
    }

    /**
     * Handles trails for the markers
     */
    fun handleTrails() {
        val newTrails = mutableObjectSetOf<Trail>()
        val hullSeq = hull.toObjectList()
        val size = hullSeq.size
        if (size < 2) {
            trails.forEach { it.close() }
            trails.clear()
            return
        }

        fun ensureTrail(a: Marker, b: Marker) {
            val trail = Trail(a, b, this, expandingProtection == null)
            trails.add(trail)
            newTrails.add(trail)
        }

        for (i in 0 until size - 1) {
            ensureTrail(hullSeq[i], hullSeq[i + 1])
        }

        // Add last trail
        if (size >= ProtectionSettings.MIN_MARKERS_LAST_CONNECTION) {
            ensureTrail(hullSeq[size - 1], hullSeq[0])
        }

        // Remove obsolete trails
        trails.removeIf { trail ->
            if (!newTrails.contains(trail)) {
                trail.close()
                true
            } else {
                trail.start()
                false
            }
        }
    }

    suspend fun finishProtection() {
        val tempRegion = tempRegion ?: return run {
            offerAccepting()
        }

        if (tempRegion.overlapsUnownedRegion(protectionUser.localPlayer)) {
            protectionUser.sendMessage(Messages.Protecting.overlappingRegions)
            return
        }

        if (tempRegion.volume <= config.area.minBlocks) {
            protectionUser.sendMessage(Messages.Protecting.areaTooSmall)
            return
        }

        val centerLoc = tempRegion.region.getFlag(Flags.TELE_LOC) ?: return run {
            protectionUser.sendMessage(Messages.Protecting.noTpPointFound)
        }

        val (pricePerBlock) = centerLoc.getProtectionPricePerBlock()
        val cost = tempRegion.effectiveVolume * pricePerBlock
        val costBD = (-cost).toBigDecimal()
        val currency = config.currency.currency

        if (!isProcessingTransaction.compareAndSet(false, true)) {
            protectionUser.sendMessage(Messages.Protecting.alreadyProcessingTransaction)
            return
        }

        try {
            if (!protectionUser.hasEnoughCurrency(costBD.abs(), currency)) {
                protectionUser.sendMessage(Messages.Protecting.tooExpensiveToBuy)
                return
            }

            val buyData = ProtectionBuyData(startLocation.world, tempRegion.region)
            val result = protectionUser.addTransaction(null, costBD, currency, buyData)

            if (result == TransactionAddResult.SUCCESS) {
                tempRegion.protect()
                removeAllMarkers()
                protectionUser.resetRegionCreation(false)
                protectionUser.bukkitPlayer?.showDialog(ProtectionCreationDialogs.protectionCreatedNotice())

                if (expandingProtection != null) {
                    ProtectionVisualizerManager.onRegionCornerChange(tempRegion.region)
                } else {
                    ProtectionVisualizerManager.onRegionCreated(
                        startLocation.world,
                        tempRegion.region
                    )
                }
            } else {
                protectionUser.sendMessage(Messages.Protecting.tooExpensiveToBuy)
            }

        } finally {
            isProcessingTransaction.set(false)
        }
    }

    /**
     * Removes all markers
     */
    suspend fun removeAllMarkers() {
        coroutineScope {
            markers.toObjectList().map { marker ->
                async {
                    marker.restorePreviousData()
                }
            }.awaitAll()
        }
        markers.clear()
        restoreHull()
        handleTrails()
    }

    /**
     * Cancel the protection
     */
    suspend fun cancelProtection() {
        removeAllMarkers()

        protectionUser.bukkitPlayer?.showDialog(ProtectionCreationDialogs.protectionCancelledNotice())
        protectionUser.resetRegionCreation(true)
    }
}