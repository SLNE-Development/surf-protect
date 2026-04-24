package dev.slne.surf.protect.paper.region.visual

import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.api.paper.nms.bridges.packets.entity.BlockDisplaySettings
import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.api.paper.visualizer.SurfPaperVisualizerApi
import dev.slne.surf.api.paper.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.region.ProtectionRegion
import org.spongepowered.math.vector.Vector3d
import java.io.Closeable
import kotlin.time.Duration.Companion.milliseconds

@Suppress("UnstableApiUsage")
@OptIn(ExperimentalVisualizerApi::class)
data class Trail(
    private val markerStart: Marker,
    private val markerEnd: Marker,
    private val protectionRegion: ProtectionRegion,
    private val isProtecting: Boolean
) : Closeable {
    private val startPos = markerStart.pos
    private val endPos = markerEnd.pos

    companion object {
        private val protectingSettings = BlockDisplaySettings {
            blockData = config.markers.creationBlockDataParsed
        }
        private val expandingSettings = BlockDisplaySettings {
            blockData = config.markers.expandingBlockDataParsed
        }
    }

    private val visualizer = SurfPaperVisualizerApi.createAreaVisualizer(
        protectionRegion.player.world,
        if (isProtecting) protectingSettings else expandingSettings,
        objectListOf(
            Vector3d(startPos.x(), startPos.y(), startPos.z()),
            Vector3d(endPos.x(), endPos.y(), endPos.z())
        ),
        useHighestYBlock = true,
        placeDelay = 50.milliseconds
    )


    fun start() {
        visualizer.startVisualizing()
        forEachPlayer { visualizer.addViewer(it) }
    }

    override fun close() {
        visualizer.stopVisualizing()
        visualizer.clearViewers()
    }
}