package dev.slne.surf.protect.paper.command.commands.protection

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.protect.paper.command.commands.protection.argument.protectionArgument
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun protectionAdminCommand() = commandTree("protectionadmin") {
    withPermission(ProtectPermissionRegistry.PROTECTION_ADMIN_COMMAND)

    literalArgument("delete") {
        withPermission(ProtectPermissionRegistry.PROTECTION_ADMIN_DELETE)
        protectionArgument("protection") {
            anyExecutor { executor, args ->
                val protection: ProtectedRegion by args

                val regionManager = protection.getRegionManagerOrNull() ?: run {
                    executor.sendText {
                        appendErrorPrefix()
                        error("Das Grundstück existiert nicht.")
                    }
                    return@anyExecutor
                }

                regionManager.removeRegion(protection.id)

                executor.sendText {
                    appendSuccessPrefix()
                    success("Das Grundstück wurde erfolgreich gelöscht.")
                }
            }
        }
    }

    literalArgument("teleport") {
        withPermission(ProtectPermissionRegistry.PROTECTION_ADMIN_TELEPORT)
        protectionArgument("protection") {
            playerExecutor { player, args ->
                val protection: ProtectedRegion by args

                val regionManager = protection.getRegionManagerOrNull() ?: run {
                    player.sendText {
                        appendErrorPrefix()
                        error("Das Grundstück existiert nicht.")
                    }
                    return@playerExecutor
                }

                player.teleportAsync(BukkitAdapter.adapt(protection.getFlag(Flags.TELE_LOC)))
                    .thenRun {
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du wurdest zum Grundstück teleportiert.")
                        }
                    }
            }
        }
    }
}