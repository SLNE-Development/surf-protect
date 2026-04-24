package dev.slne.surf.protect.paper.command

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.api.paper.visualizer.visualizer.ExperimentalVisualizerApi
import dev.slne.surf.api.paper.visualizer.visualizer.SurfVisualizerArea
import dev.slne.surf.protect.paper.command.commands.protection.migrateFlagCommand
import dev.slne.surf.protect.paper.command.commands.protection.protectionAdminCommand
import dev.slne.surf.protect.paper.command.commands.protection.protectionCommand
import dev.slne.surf.protect.paper.command.commands.protectionWhoCommand
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
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
        protectionAdminCommand()
    }
}