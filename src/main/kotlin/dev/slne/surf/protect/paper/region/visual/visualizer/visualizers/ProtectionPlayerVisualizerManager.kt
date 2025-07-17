package dev.slne.surf.protect.paper.region.visual.visualizer.visualizers

import com.github.benmanes.caffeine.cache.Caffeine
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionType
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.visual.visualizer.color.VisualizerColor
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.packets.entity.BlockDisplaySettings
import dev.slne.surf.surfapi.bukkit.api.visualizer.surfVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.SurfVisualizerArea
import org.bukkit.World
import org.bukkit.entity.Player
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.math.vector.Vector3f
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalVisualizerApi::class)
class ProtectionPlayerVisualizerManager(val uuid: UUID) {
    private val cache = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .removalListener<String, SurfVisualizerArea> { id, visualizer, cause ->
            if (cause.wasEvicted()) {
                visualizer?.stopVisualizing()
            }
        }
        .build<String, SurfVisualizerArea>()

    private val visualizing = AtomicBoolean(false)

    fun isVisualizing(): Boolean {
        return visualizing.get()
    }

    fun start() {
        if (visualizing.getAndSet(true)) return
        val player = server.getPlayer(uuid) ?: return
        for (world in server.worlds) {
            val regionManager = world.getRegionManagerOrNull() ?: continue
            val regions = regionManager.regions.values
            for (region in regions) {
                if (region.type == RegionType.GLOBAL) continue
                if (region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE) == StateFlag.State.DENY) continue
                val visualizer = getOrCreateVisualizer(region, world, player)
                visualizer.setCornerLocations(region.points.map {
                    Vector3d(
                        it.x().toDouble(),
                        0.0,
                        it.z().toDouble()
                    )
                })
                visualizer.addViewer(player)
                visualizer.startVisualizing()
            }
        }
    }

    fun stop() {
        for (visualizer in cache.asMap().values) {
            visualizer.stopVisualizing()
        }
        visualizing.set(false)
    }

    fun onRegionCreated(world: World, region: ProtectedRegion) {
        if (!isVisualizing()) return
        val player = server.getPlayer(uuid) ?: return
        if (region.type == RegionType.GLOBAL) return
        if (region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE) == StateFlag.State.DENY) return

        val visualizer = getOrCreateVisualizer(region, world, player)
        visualizer.setCornerLocations(region.points.map {
            Vector3d(
                it.x().toDouble(),
                0.0,
                it.z().toDouble()
            )
        })

        visualizer.addViewer(player)
        visualizer.startVisualizing()
        cache.put(region.id, visualizer)
    }

    fun onRegionCornerChange(region: ProtectedRegion) {
        val visualizer = cache.getIfPresent(region.id) ?: return
        visualizer.setCornerLocations(region.points.map {
            Vector3d(
                it.x().toDouble(),
                0.0,
                it.z().toDouble()
            )
        })
    }

    fun onRegionMemberChange(region: ProtectedRegion) {
        val visualizer = cache.getIfPresent(region.id) ?: return
        val player = server.getPlayer(uuid) ?: return

        visualizer.settings {
            blockData = VisualizerColor.selectColor(player, region).blockData
        }
    }

    fun onRegionDeletion(region: ProtectedRegion) {
        val visualizer = cache.getIfPresent(region.id) ?: return
        visualizer.stopVisualizing()
        cache.invalidate(region.id)
    }

    fun update(region: ProtectedRegion) {
        val visualizer = cache.getIfPresent(region.id) ?: return
        val player = server.getPlayer(uuid) ?: return

        visualizer.settings {
            blockData = VisualizerColor.selectColor(player, region).blockData
            scale = Vector3f(1f, 5f, 1f)
        }

        visualizer.setCornerLocations(region.points.map {
            Vector3d(
                it.x().toDouble(),
                0.0,
                it.z().toDouble()
            )
        })
    }

    private fun getOrCreateVisualizer(
        region: ProtectedRegion,
        world: World,
        player: Player
    ): SurfVisualizerArea =
        cache.get(region.id) {
            surfVisualizerApi.createAreaVisualizer(
                world = world,
                initialSettings = BlockDisplaySettings {
                    scale = Vector3f(1f, 5f, 1f)
                    blockData = VisualizerColor.selectColor(player, region).blockData
                },
                useHighestYBlock = true
            )
        }

}