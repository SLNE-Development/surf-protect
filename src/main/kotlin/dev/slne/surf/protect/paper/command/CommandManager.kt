package dev.slne.surf.protect.paper.command

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.multiLiteralArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.protect.paper.command.commands.protection.migrateFlagCommand
import dev.slne.surf.protect.paper.command.commands.protection.protectionCommand
import dev.slne.surf.protect.paper.command.commands.protectionWhoCommand
import dev.slne.surf.protect.paper.region.visual.visualizer.color.VisualizerColor
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.packets.entity.BlockDisplaySettings
import dev.slne.surf.surfapi.bukkit.api.visualizer.surfVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.surfapi.bukkit.api.visualizer.visualizer.SurfVisualizerArea
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import org.spongepowered.math.vector.Vector3d
import org.spongepowered.math.vector.Vector3f
import java.util.*
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalVisualizerApi::class)
object CommandManager {
    private val visualizers = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .build<UUID, Object2ObjectMap<String, SurfVisualizerArea>>()

    fun registerCommands() {
        protectionWhoCommand()
        protectionCommand()
        migrateFlagCommand()

        commandAPICommand("protection-visualize") {
            stringArgument("id") {
                replaceSuggestions(ArgumentSuggestions.stringCollection { context ->
                    server.worlds.mapNotNull { it.getRegionManagerOrNull() }
                        .flatMap { it.regions.values }
                        .map { it.id }
                })
            }
            multiLiteralArgument("action", "start", "stop")

            playerExecutor { sender, args ->
                val id: String by args
                val action: String by args
                val region = sender.world.getRegionManagerOrNull()
                    ?.regions?.values?.find { it.id.equals(id, ignoreCase = true) }

                if (region == null) {
                    throw CommandAPI.failWithString("Region with ID '$id' not found.")
                }

                val visualizer =
                    visualizers.asMap().getOrPut(sender.uniqueId) { mutableObject2ObjectMapOf() }
                val currentVisualizer = visualizer[id]

                if (action == "start") {
                    if (currentVisualizer != null) {
                        currentVisualizer.removeViewer(sender)
                        if (currentVisualizer.isVisualizing()) {
                            currentVisualizer.stopVisualizing()
                        }
                    }

                    val newVisualizer = surfVisualizerApi.createAreaVisualizer(
                        sender.world,
                        BlockDisplaySettings {
                            scale = Vector3f(1f, 5f, 1f)
                            blockData = VisualizerColor.selectColor(sender, region).blockData
                        },
                        region.points.map { Vector3d(it.x().toDouble(), 0.0, it.z().toDouble()) },
                        true
                    )

                    newVisualizer.addViewer(sender)
                    newVisualizer.startVisualizing()
                    visualizer[id] = newVisualizer
                } else {
                    if (currentVisualizer == null) {
                        throw CommandAPI.failWithString("No visualizer found for region '$id'.")
                    }

                    currentVisualizer.removeViewer(sender)
                    if (currentVisualizer.isVisualizing()) {
                        currentVisualizer.stopVisualizing()
                    }
                    visualizer.remove(id)
                }
            }
        }
    }
}