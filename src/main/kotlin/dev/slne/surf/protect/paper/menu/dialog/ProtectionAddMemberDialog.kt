package dev.slne.surf.protect.paper.menu.dialog

import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.members.ProtectionMemberListView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.surfapi.bukkit.api.dialog.search.searchDialog
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
fun protectionAddMemberDialog(protection: RegionInfo) = searchDialog(
    title = {
        protectColored("Spieler hinzufügen...")
    },
    searchInput = {

    },
    body = {
        plainMessage {
            protectColored("Gib den Namen des Spielers ein, den du hinzufügen möchtest.")
            appendNewline()
            appendWarningPrefix()
            error("Der Spieler muss bereits einmal auf diesem Server gespielt haben, damit er hinzugefügt werden kann.")
        }
    },
    onSearch = { player, query ->
        handleAdd(player, query, protection)
    },
    onClose = { player, query ->
        handleAdd(player, query, protection)
    }
)

private fun handleAdd(player: Player, playerName: String, protection: RegionInfo) {
    if (playerName.isEmpty() || playerName.isBlank() || playerName.length > 16) {
        player.sendText {
            appendErrorPrefix()
            error("Der Spielername ist ungültig.")
        }
        return
    }


    val target = Bukkit.getOfflinePlayer(playerName)

    if (!target.hasPlayedBefore()) {
        player.sendText {
            appendErrorPrefix()
            error("Der Spieler wurde nicht gefunden.")
        }
    }

    if (protection.members.map { it.uniqueId }.contains(target.uniqueId)) {
        player.sendText {
            appendErrorPrefix()
            error("Der Spieler ist bereits Mitglied dieses Grundstücks.")
        }
    }

    protection.region.members.addPlayer(target.uniqueId)
    ProtectionVisualizerManager.onRegionMemberChange(protection.region)

    player.sendText {
        appendSuccessPrefix()
        success("Der Spieler wurde erfolgreich als Mitglied hinzugefügt.")
    }

    player.closeDialog()
    viewFrame.open(ProtectionMemberListView::class.java, player, mapOf("protection" to protection))
}