package dev.slne.surf.protect.paper.command.commands.protection

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame

fun protectionCommand() = commandAPICommand("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)

    playerExecutorSuspend { sender, _ ->
        viewFrame.open(ProtectionListView::class.java, sender)
    }
}