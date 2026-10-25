@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.region

import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.RegionGroup
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.*
import dev.slne.surf.api.paper.util.getHighestBlockYAtBlockCoordinates
import dev.slne.surf.api.paper.util.getXFromChunkKey
import dev.slne.surf.api.paper.util.getZFromChunkKey
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.event.ProtectionCreateEvent
import dev.slne.surf.protect.paper.math.Mth
import dev.slne.surf.protect.paper.message.Messages
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.EditableProtectionFlags
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionCreationState
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.region.structure.StructureFootprint
import dev.slne.surf.protect.paper.region.structure.StructureSurcharge
import dev.slne.surf.protect.paper.region.visual.Marker
import dev.slne.surf.protect.paper.region.visual.QuickHull
import dev.slne.surf.protect.paper.region.visual.Trail
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.*
import io.papermc.paper.math.BlockPosition
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min
import kotlin.math.roundToInt
import com.sk89q.worldedit.util.Location as WorldEditLocation

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

    private val regionMinY: Int = expandingProtection?.minimumPoint?.y() ?: player.world.let { world ->
        if (world.environment == World.Environment.NETHER && config.protection.protectOnlyAboveNetherRoofInNether) {
            world.logicalHeight
        } else {
            world.minHeight
        }
    }
    private val regionMaxY: Int = expandingProtection?.maximumPoint?.y() ?: (player.world.maxHeight - 1)
    private val structureFootprint = StructureFootprint(player.world, regionMinY, regionMaxY)

    /** Increased whenever the region changes, so offers calculated for an outdated region are dropped. */
    private val offerVersion = AtomicInteger()

    @Volatile
    private var lastOffer: OfferedCost? = null

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
            val namespacedKey = Chunk.getChunkKey(point.x() shr 4, point.z() shr 4)
            val list = byChunk.computeIfAbsent(namespacedKey) { mutableObjectListOf() }
            list.add(point)
        }

        val snapshots = coroutineScope {
            byChunk.keys.mapAsync { namespacedKey ->
                namespacedKey to world.getChunkAtAsync(
                    getXFromChunkKey(namespacedKey),
                    getZFromChunkKey(namespacedKey)
                )
                    .await()
                    .getChunkSnapshot(true, false, false, false)
            }.toMap(mutableLong2ObjectMapOf<ChunkSnapshot>(byChunk.size))
        }

        val it = byChunk.long2ObjectEntrySet().fastIterator()
        while (it.hasNext()) {
            val entry = it.next()
            val namespacedKey = entry.longKey
            val pointsInChunk = entry.value
            val snapshot = snapshots[namespacedKey]
                ?: error("ChunkSnapshot for namespacedKey $namespacedKey not found")
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
        val world = player.world
        if (world.environment == World.Environment.NETHER && config.protection.protectOnlyAboveNetherRoofInNether) {
            if (pos.y() < world.logicalHeight) {
                player.sendMessage(Messages.Protecting.canOnlyProtectAboveNetherRoofInNether)
                return null
            }
        }

        val candidate = Marker(WeakReference(world), this, pos, previousData)
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
    private fun getDiscountFactor(): Double {
        return if (player.hasPermission(ProtectPermissionRegistry.PROTECTION_DISCOUNT)) {
            config.pricing.discountModifier
        } else {
            1.0
        }
    }

    private fun offerAccepting(): RegionCreationState {
        offerVersion.incrementAndGet()
        lastOffer = null

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
                regionMinY,
                regionMaxY
            )
            region.copyFrom(expandingProtection)
        } else {
            val name = player.name + "-" + RandomStringUtils.secureStrong()
                .nextAlphabetic(ProtectionSettings.RANDOM_NAME_LENGTH)
                .uppercase()

            region = ProtectedPolygonalRegion(
                name,
                vectors,
                regionMinY,
                regionMaxY
            )
            region.owners.addPlayer(protectionUser.localPlayer)

            for (flagsMap in EditableProtectionFlags.entries) {
                if (flagsMap.isPlayerRelated) {
                    region.setFlag(flagsMap.flag, StateFlag.State.ALLOW)
                    if ((flagsMap.initialState ?: StateFlag.State.ALLOW) == StateFlag.State.DENY) {
                        region.setFlag(flagsMap.flag.regionGroupFlag, RegionGroup.MEMBERS)
                    } else {
                        region.setFlag(flagsMap.flag.regionGroupFlag, null)
                    }
                } else {
                    region.setFlag(flagsMap.flag, flagsMap.initialState)
                }
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
            tmpRegion.effectiveVolume = region.addedArea(expandingProtection)
        }

        return when {
            tmpRegion.overlapsUnownedRegion(protectionUser.localPlayer) -> RegionCreationState.OVERLAPPING.also {
                protectionUser.sendMessage(Messages.Protecting.overlappingRegions)
            }

            tmpVolume <= config.area.minBlocks -> RegionCreationState.TOO_SMALL.also {
                protectionUser.sendMessage(Messages.Protecting.areaTooSmall)
            }

            tmpVolume > config.area.maxBlocks -> RegionCreationState.TOO_LARGE.also {
                protectionUser.sendMessage(Messages.Protecting.areaTooBig)
            }

            else -> {
                // The structure surcharge needs chunks to be loaded, so it is added in sendOfferAsync
                val (baseCost, _, spawnDistance) = Mth.calculateEffectiveCost(
                    centerLoc,
                    tmpRegion,
                    getDiscountFactor()
                )

                if (baseCost <= 0) {
                    protectionUser.sendMessage(Messages.Protecting.areaTooSmall)
                    RegionCreationState.TOO_SMALL
                } else {
                    if (spawnDistance < config.pricing.spawnProtectionPerBlock) {
                        protectionUser.sendMessage(buildText {
                            appendErrorPrefix()
                            error("Das Grundstück liegt zu nah am Spawn.")
                        })
                        return RegionCreationState.TOO_NEAR_FROM_SPAWN
                    }

                    sendOfferAsync(tmpRegion, centerLoc)
                    RegionCreationState.SUCCESS
                }
            }
        }
    }

    /**
     * Calculates the offer including the structure surcharge and sends it, unless the region
     * changed in the meantime.
     */
    private fun sendOfferAsync(tmpRegion: TempProtectionRegion, centerLoc: WorldEditLocation) {
        val version = offerVersion.get()
        plugin.launch {
            val offer = calculateOfferOrNull(tmpRegion, centerLoc) ?: return@launch
            if (offerVersion.get() != version) return@launch
            sendOffer(offer, version)
        }
    }

    private fun sendOffer(offer: Offer, version: Int) {
        val (effectiveCost, pricePerBlock, spawnDistance, discountFactor) = offer.cost
        lastOffer = OfferedCost(version, effectiveCost)

        protectionUser.sendMessage(
            Messages.Protecting.offer(
                offer.region.volume,
                effectiveCost,
                config.currency.currency,
                pricePerBlock,
                spawnDistance,
                discountFactor,
                offer.structureSurcharge,
                offer.region.effectiveVolume
            )
        )
    }

    private suspend fun calculateOffer(tmpRegion: TempProtectionRegion, centerLoc: WorldEditLocation): Offer {
        val structureSurcharge = structureFootprint.calculateSurcharge(tmpRegion.region, expandingProtection)
        val cost = Mth.calculateEffectiveCost(
            centerLoc,
            tmpRegion,
            getDiscountFactor(),
            structureSurcharge.totalCost
        )

        return Offer(tmpRegion, cost, structureSurcharge)
    }

    /**
     * Calculates the offer, or tells the player that it failed, e.g. because a chunk could not be loaded.
     */
    private suspend fun calculateOfferOrNull(tmpRegion: TempProtectionRegion, centerLoc: WorldEditLocation): Offer? {
        return try {
            calculateOffer(tmpRegion, centerLoc)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to calculate the protection offer for ${player.name}", e)
            protectionUser.sendMessage(Messages.Protecting.offerCalculationFailed)
            null
        }
    }

    private class Offer(
        val region: TempProtectionRegion,
        val cost: Mth.EffectiveCostResult,
        val structureSurcharge: StructureSurcharge
    )

    private data class OfferedCost(val version: Int, val cost: Double)

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

        // Recalculate before paying, so the player never pays a price that differs from the last offer
        val version = offerVersion.get()
        val offeredCost = lastOffer?.takeIf { it.version == version }?.cost
        val offer = calculateOfferOrNull(tempRegion, centerLoc) ?: return
        if (offerVersion.get() != version) return // the region changed while calculating

        val costResult = offer.cost
        if (offeredCost != costResult.effectiveCost) {
            if (offeredCost != null) {
                protectionUser.sendMessage(Messages.Protecting.offerChanged)
            }
            sendOffer(offer, version)
            return
        }

        val cost = costResult.effectiveCost.roundToInt()
        val costBD = (-cost).toBigDecimal()
        val currency = config.currency.currency

        if (costResult.pricePerBlock == Double.MAX_VALUE) {
            protectionUser.sendMessage(buildText {
                appendErrorPrefix()
                error("Das Grundstück liegt zu nah am Spawn.")
            })
            return
        }

        if (!isProcessingTransaction.compareAndSet(false, true)) {
            protectionUser.sendMessage(Messages.Protecting.alreadyProcessingTransaction)
            return
        }

        try {
            if (protectionUser.transactionUser.balance(currency) < costBD.abs()) {
                protectionUser.sendMessage(Messages.Protecting.tooExpensiveToBuy)
                return
            }

            val result = protectionUser.transactionUser.withdraw(costBD, currency, false)

            if (result.success) {
                tempRegion.protect()
                removeAllMarkers()
                protectionUser.resetRegionCreation(false)
                protectionUser.bukkitPlayer?.sendText {
                    appendSuccessPrefix()
                    success("Das Grundstück wurde erstellt.")
                }

                if (expandingProtection != null) {
                    ProtectionVisualizerManager.onRegionCornerChange(tempRegion.region)
                } else {
                    ProtectionVisualizerManager.onRegionCreated(
                        startLocation.world,
                        tempRegion.region
                    )

                    applyDefaultFlags(tempRegion.region)
                }

                withContext(plugin.globalRegionDispatcher) {
                    protectionUser.bukkitPlayer?.let {
                        ProtectionCreateEvent(it).callEvent()
                    }
                }
            } else {
                protectionUser.sendMessage(Messages.Protecting.tooExpensiveToBuy)
            }

        } finally {
            isProcessingTransaction.set(false)
        }
    }

    private fun applyDefaultFlags(region: ProtectedRegion) {
        for (flag in EditableProtectionFlags.entries) {

            val state = flag.initialState ?: StateFlag.State.ALLOW

            if (flag.isPlayerRelated) {
                region.setFlag(flag.flag, StateFlag.State.ALLOW)
                if (state == StateFlag.State.DENY) {
                    region.setFlag(flag.flag.regionGroupFlag, RegionGroup.MEMBERS)
                } else {
                    region.setFlag(flag.flag.regionGroupFlag, null)
                }
                continue
            }

            region.setFlag(flag.flag, state)
        }
    }

    /**
     * Removes all markers
     */
    suspend fun removeAllMarkers(shutdown: Boolean = false) {
        coroutineScope {
            markers.toObjectList().map { marker ->
                async {
                    marker.restorePreviousData(shutdown)
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
    suspend fun cancelProtection(shutdown: Boolean = false) {
        removeAllMarkers(shutdown)

        protectionUser.bukkitPlayer?.sendText {
            appendInfoPrefix()
            info("Der Grundstückserstellungsprozess wurde abgebrochen.")
        }

        protectionUser.resetRegionCreation(true, shutdown)
    }
}