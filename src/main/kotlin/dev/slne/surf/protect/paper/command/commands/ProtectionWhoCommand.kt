package dev.slne.surf.protect.paper.command.commands

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.protect.paper.message.Messages
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.getProtectedRegions
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.Component

fun protectionWhoCommand() = commandAPICommand("pwho") {
    withPermission(ProtectPermissionRegistry.PROTECTION_WHO_COMMAND)
    playerExecutor { player, _ ->
        val loc = player.location
        val regions = loc.getProtectedRegions()

        if (regions.isEmpty()) {
            throw CommandAPI.failWithString(Messages.Command.PWho.NO_PLAYER_DEFINED_REGION)
        }

        player.sendText {
            appendCollectionNewLine(regions, Component.empty()) { region ->
                Messages.Command.PWho.renderInfo(RegionInfo(region))
            }
        }
    }
}