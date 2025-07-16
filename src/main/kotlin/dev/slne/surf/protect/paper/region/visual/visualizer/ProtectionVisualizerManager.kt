package dev.slne.surf.protect.paper.region.visual.visualizer

import com.github.benmanes.caffeine.cache.Caffeine
import com.ibm.icu.impl.ValidIdentifiers
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionType
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.visual.visualizer.color.VisualizerColor
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.surf.surfapi.bukkit.api.event.listen
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.visualizer.surfVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.SurfVisualizerArea
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.math.vector.Vector3f
import java.util.*
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalVisualizerApi::class)
object ProtectionVisualizerManager { // TODO: 13.07.2025 17:17 - add region
    private val visualizers = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .build<UUID, Object2ObjectMap<String, SurfVisualizerArea>>()

    init {
        listen<PlayerQuitEvent> {
            visualizers.invalidate(player.uniqueId) // visualizers should automatically gc because they are weakly referenced
        }
    }

    fun isVisualizing(player: Player): Boolean {
        return visualizers.asMap().containsKey(player.uniqueId)
    }

    fun switchVisualizing(player: Player): Boolean {
        return if (isVisualizing(player)) {
            stopVisualizer(player)
            false
        } else {
            startVisualizer(player)
            true
        }
    }

    fun stopVisualizer(player: Player) {
        val playerVisualizers = visualizers.asMap().remove(player.uniqueId) ?: return
        for (visualizer in playerVisualizers.values) {
            visualizer.removeViewer(player)
            if (!visualizer.isVisualizing()) {
                visualizer.stopVisualizing()
            }
        }
    }

    fun startVisualizer(player: Player) {
        if (visualizers.asMap().containsKey(player.uniqueId)) {
            stopVisualizer(player) // clear existing visualizers
        }

        val playerVisualizers = mutableObject2ObjectMapOf<String, SurfVisualizerArea>()
        for (world in server.worlds) {
            val rm = world.getRegionManagerOrNull() ?: continue
            for (region in rm.regions.values) {
                if (region.type == RegionType.GLOBAL) continue
                if (region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE) == StateFlag.State.DENY) continue
                val visualizer =
                    surfVisualizerApi.createAreaVisualizer(world, useHighestYBlock = true)
                updateVisualizer(player, region, visualizer)
                visualizer.addViewer(player)
                visualizer.startVisualizing()
                playerVisualizers[region.id] = visualizer
            }
        }
        visualizers.put(player.uniqueId, playerVisualizers)
    }

    fun updateVisualizer(region: ProtectedRegion) {
        visualizers.asMap().forEach { (playerUuid, regionMap) ->
            regionMap[region.id]?.takeIf { it.viewers.isNotEmpty() }?.let { visualizer ->
                updateVisualizer(visualizer.viewers.first(), region, visualizer)
            }
        }
    }

    fun stopVisualizer(region: ProtectedRegion) {
        visualizers.asMap().forEach { (playerUuid, regionMap) ->
            val visualizer = regionMap.remove(region.id)
            if (visualizer != null) {
                visualizer.stopVisualizing()
                if (regionMap.isEmpty()) {
                    visualizers.invalidate(playerUuid)
                }
            }
        }
    }

    private fun updateVisualizer(
        player: Player,
        region: ProtectedRegion,
        visualizer: SurfVisualizerArea
    ) {
        val corners = region.points.map { Vector3d(it.x().toDouble(), 0.0, it.z().toDouble()) }
        visualizer.settings {
            blockData = VisualizerColor.selectColor(player, region).blockState
            scale = Vector3f(1.0f, 5.0f, 1.0f)
        }
        visualizer.setCornerLocations(corners)
    }
}