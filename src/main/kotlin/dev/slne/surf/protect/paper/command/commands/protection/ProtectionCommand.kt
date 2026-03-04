package dev.slne.surf.protect.paper.command.commands.protection

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.optionalArgument
import dev.slne.surf.protect.paper.dialogs.ProtectionMainDialog
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.bukkit.api.command.util.awaitAsyncPlayerProfileOptional
import dev.slne.surf.surfapi.bukkit.api.command.util.idOrThrow
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit

fun protectionCommand() = commandAPICommand("protect") {
    withPermission(ProtectPermissionRegistry.PROTECTION_COMMAND)
    optionalArgument(AsyncPlayerProfileArgument("player"))

    playerExecutorSuspend { sender, args ->
        val player = args.awaitAsyncPlayerProfileOptional("player")

        if(player == null) {
            sender.showDialog(ProtectionMainDialog.mainDialog(sender, sender))
            return@playerExecutorSuspend
        }

        sender.showDialog(ProtectionMainDialog.mainDialog(sender, Bukkit.getOfflinePlayer(player.idOrThrow())))
    }
}