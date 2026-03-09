package dev.slne.surf.protect.paper.command.commands.protection

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame

fun protectionCommand() = commandTree("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)

    playerExecutor { sender, args ->
        viewFrame.open(ProtectionListView::class.java, sender)
    }
}