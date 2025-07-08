@file:Suppress("UnstableApiUsage")

package dev.slne.protect.paper.dialogs.sub

import dev.slne.protect.paper.dialogs.ProtectionMainDialog
import dev.slne.protect.paper.region.ProtectionUtils
import dev.slne.protect.paper.region.info.RegionInfo
import dev.slne.protect.paper.user.ProtectionUser
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object ProtectionListDialog {
    fun listProtectionsDialog(viewer: Player, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protections — Grundstücke") }
            afterAction(DialogBase.DialogAfterAction.NONE)
        }
        type {
            val protectionList = createProtectionList(viewer, target)
            dialogList(*protectionList.toTypedArray()) {
                columns(3)
                buttonWidth(125)
                exitAction(createExitAction(target))
            }
        }
    }

    private fun createProtectionList(viewer: Player, target: OfflinePlayer): List<Dialog> =
        ProtectionUtils.getRegionListFor(ProtectionUser.getProtectionUser(target).localPlayer)
            .asSequence()
            .map { (world, regions) -> regions.map { RegionInfo(world, it) } }
            .flatten()
            .map { ProtectionInfoDialog.createProtectionInfoDialog(viewer, it, target) }
            .toList()

    private fun createExitAction(target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { it.showDialog(ProtectionMainDialog.mainDialog(it, target)) }
        }
    }
}