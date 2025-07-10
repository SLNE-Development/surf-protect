package dev.slne.surf.protect.paper.command.commands.protection

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.asyncOfflinePlayerArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.protect.paper.dialogs.ProtectionMainDialog
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.plugin
import kotlinx.coroutines.future.await
import org.bukkit.OfflinePlayer
import java.util.concurrent.CompletableFuture

fun protectionCommand() = commandAPICommand("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)
    asyncOfflinePlayerArgument("player", optional = true)

    playerExecutor { sender, args ->
        val playerFuture = args.getUnchecked<CompletableFuture<OfflinePlayer>>("player")
        if (playerFuture != null) {
            plugin.launch {
                val player = playerFuture.await()
                sender.showDialog(ProtectionMainDialog.mainDialog(sender, player))
            }
        } else {
            sender.showDialog(ProtectionMainDialog.mainDialog(sender, sender))
        }
    }
}