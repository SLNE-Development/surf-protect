package dev.slne.surf.protect.paper.command.commands.protection

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.protect.paper.menu.view.protectionMainView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.user.protectionUser

fun protectionCommand() = commandTree("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)

    playerExecutor { sender, args ->
        // The menu allows selling or expanding protections, which must not happen while one is being edited
        if (sender.protectionUser().isCreatingRegion) {
            sender.sendText {
                appendErrorPrefix()
                error("Du kannst das Menü im Protection-Mode nicht öffnen.")
            }
            return@playerExecutor
        }

        protectionMainView.open(sender)
    }
}
