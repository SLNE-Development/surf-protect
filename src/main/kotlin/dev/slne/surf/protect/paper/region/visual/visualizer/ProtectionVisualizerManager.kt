package dev.slne.surf.protect.paper.region.visual.visualizer

import com.github.benmanes.caffeine.cache.Caffeine
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.region.visual.visualizer.visualizers.ProtectionPlayerVisualizerManager
import dev.slne.surf.surfapi.bukkit.api.event.listen
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.ExperimentalVisualizerApi
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

@OptIn(ExperimentalVisualizerApi::class)
object ProtectionVisualizerManager { // TODO: 13.07.2025 17:17 - add region
    private val visualizers = Caffeine.newBuilder()
        .build<UUID, ProtectionPlayerVisualizerManager> { ProtectionPlayerVisualizerManager(it) }

    init {
        listen<PlayerQuitEvent> {
            stop(player)
        }
    }

    fun isVisualizing(player: Player): Boolean {
        return visualizers.getIfPresent(player.uniqueId)?.isVisualizing() == true
    }

    fun switchVisualizing(player: Player): Boolean {
        return if (isVisualizing(player)) {
            stop(player)
            false
        } else {
            startVisualizer(player)
            true
        }
    }

    fun stop(player: Player) {
        visualizers.getIfPresent(player.uniqueId)?.stop()
    }

    fun startVisualizer(player: Player) {
        visualizers.get(player.uniqueId).start()
    }

    fun updateVisualizer(region: ProtectedRegion) {
        for (manager in visualizers.asMap().values) {
            manager.update(region)
        }
    }

    fun onRegionDeletion(region: ProtectedRegion) {
        for (manager in visualizers.asMap().values) {
            manager.onRegionDeletion(region)
        }
    }

    fun onRegionCreated(world: World, region: ProtectedRegion) {
        for (manager in visualizers.asMap().values) {
            manager.onRegionCreated(world, region)
        }
    }

    fun onRegionCornerChange(region: ProtectedRegion) {
        for (manager in visualizers.asMap().values) {
            manager.onRegionCornerChange(region)
        }
    }

    fun onRegionMemberChange(region: ProtectedRegion) {
        for (manager in visualizers.asMap().values) {
            manager.onRegionMemberChange(region)
        }
    }
}