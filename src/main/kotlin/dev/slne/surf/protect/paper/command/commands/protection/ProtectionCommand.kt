package dev.slne.surf.protect.paper.command.commands.protection

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.protect.paper.menu.view.ProtectionMainView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry

fun protectionCommand() = commandTree("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)

    playerExecutor { sender, args ->
        viewFrame.open(ProtectionMainView::class.java, sender)
    }
}