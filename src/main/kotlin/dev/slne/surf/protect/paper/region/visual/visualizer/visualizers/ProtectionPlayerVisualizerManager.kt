package dev.slne.surf.protect.paper.region.visual.visualizer.visualizers

import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionType
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.api.paper.nms.bridges.packets.entity.BlockDisplaySettings
import dev.slne.surf.api.paper.visualizer.SurfPaperVisualizerApi
import dev.slne.surf.api.paper.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.api.paper.visualizer.visualizer.SurfVisualizerArea
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.visual.visualizer.color.VisualizerColor
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import org.bukkit.World
import org.bukkit.entity.Player
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.math.vector.Vector3f
import java.util.*

@OptIn(ExperimentalVisualizerApi::class)
class ProtectionPlayerVisualizerManager(val uuid: UUID) {
    companion object {
        private val log = logger()
    }

    private val activeVisualizers = mutableObject2ObjectMapOf<String, SurfVisualizerArea>()

    private val lock = Any()
    private var visualizing = false

    fun isVisualizing(): Boolean = synchronized(lock) {
        visualizing
    }

    fun start() = synchronized(lock) {
        if (visualizing) return@synchronized
        val player = server.getPlayer(uuid) ?: return@synchronized
        visualizing = true

        for (world in server.worlds) {
            val regionManager = world.getRegionManagerOrNull() ?: continue
            val regions = regionManager.regions.values
            for (region in regions) {
                if (region.type == RegionType.GLOBAL) continue
                if (region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE) == StateFlag.State.DENY) continue
                createAndStartVisualizer(region, world, player)
            }
        }
    }

    fun stop() = synchronized(lock) {
        if (!visualizing) return@synchronized
        visualizing = false

        for (visualizer in activeVisualizers.values) {
            visualizer.close()
        }
        activeVisualizers.clear()
    }

    private fun createAndStartVisualizer(
        region: ProtectedRegion,
        world: World,
        player: Player
    ) {
        val visualizer = SurfPaperVisualizerApi.createAreaVisualizer(
            world = world,
            initialSettings = BlockDisplaySettings {
                scale = Vector3f(1f, 5f, 1f)
                blockData = VisualizerColor.selectColor(player, region).blockData
            },
            useHighestYBlock = true
        )

        visualizer.setCornerLocations(region.points.map {
            Vector3d(it.x().toDouble(), 0.0, it.z().toDouble())
        })
        visualizer.addViewer(player)
        visualizer.startVisualizing()

        val current = activeVisualizers.put(region.id, visualizer)

        if (current != null) {
            current.close()
            log.atWarning()
                .log("A visualizer for region ${region.id} already exists for player ${player.name}. This should not happen, but the old visualizer has been replaced with a new one.")
        }
    }

    fun onRegionDeletion(region: ProtectedRegion) = synchronized(lock) {
        val visualizer = activeVisualizers.remove(region.id) ?: return@synchronized
        visualizer.close()
    }

    fun onRegionCreated(world: World, region: ProtectedRegion) = synchronized(lock) {
        if (!visualizing) return@synchronized
        val player = server.getPlayer(uuid) ?: return@synchronized
        if (region.type == RegionType.GLOBAL) return@synchronized
        if (region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE) == StateFlag.State.DENY) return@synchronized

        createAndStartVisualizer(region, world, player)
    }

    fun onRegionCornerChange(region: ProtectedRegion) = synchronized(lock) {
        val visualizer = activeVisualizers[region.id] ?: return@synchronized
        visualizer.setCornerLocations(region.points.map {
            Vector3d(it.x().toDouble(), 0.0, it.z().toDouble())
        })
    }

    fun onRegionMemberChange(region: ProtectedRegion) = synchronized(lock) {
        val visualizer = activeVisualizers[region.id] ?: return@synchronized
        val player = server.getPlayer(uuid) ?: return@synchronized
        visualizer.settings {
            blockData = VisualizerColor.selectColor(player, region).blockData
        }
    }

    fun update(region: ProtectedRegion) = synchronized(lock) {
        val visualizer = activeVisualizers[region.id] ?: return@synchronized
        val player = server.getPlayer(uuid) ?: return@synchronized

        visualizer.settings {
            blockData = VisualizerColor.selectColor(player, region).blockData
            scale = Vector3f(1f, 5f, 1f)
        }

        visualizer.setCornerLocations(region.points.map {
            Vector3d(it.x().toDouble(), 0.0, it.z().toDouble())
        })
    }
}