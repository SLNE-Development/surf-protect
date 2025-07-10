package dev.slne.surf.protect.paper.command

import dev.slne.surf.protect.paper.command.commands.protection.migrateFlagCommand
import dev.slne.surf.protect.paper.command.commands.protection.protectionCommand
import dev.slne.surf.protect.paper.command.commands.protectionWhoCommand

object CommandManager {
    fun registerCommands() {
        protectionWhoCommand()
        protectionCommand()
        migrateFlagCommand()
    }
}