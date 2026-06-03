package dev.slne.surf.protect.paper.command.commands.protection

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.protect.paper.menu.view.protectionMainView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry

fun protectionCommand() = commandTree("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)

    playerExecutor { sender, args ->
        protectionMainView.open(sender)
    }
}