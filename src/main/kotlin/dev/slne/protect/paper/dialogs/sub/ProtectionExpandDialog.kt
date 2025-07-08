@file:Suppress("UnstableApiUsage")

package dev.slne.protect.paper.dialogs.sub

import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.protect.paper.region.ProtectionRegion
import dev.slne.protect.paper.region.ProtectionUtils
import dev.slne.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.protect.paper.region.info.RegionInfo
import dev.slne.protect.paper.user.ProtectionUser
import dev.slne.surf.surfapi.bukkit.api.dialog.*
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.OfflinePlayer

object ProtectionExpandDialog {

    fun createProtectionExpandConfirmationDialog(info: RegionInfo, target: OfflinePlayer): Dialog =
        dialog {
            base {
                title { primary("Protection — Erweitern") }
                afterAction(DialogBase.DialogAfterAction.NONE)
                body {
                    plainMessage { info("Bist du dir sicher, dass du das Grundstück erweitern möchtest?") }
                }
            }
            type {
                confirmation(createYesButton(info), createNoButton(info, target))
            }
        }

    private fun createYesButton(info: RegionInfo) = actionButton {
        label { success("Ja") }
        action {
            playerCallback { viewer ->
                val region = info.region
                val canSellState = region.getFlag(ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG)
                val canExpandState = canSellState != StateFlag.State.DENY
                if (!canExpandState) {
                    viewer.showDialog(createCannotExpandNotice())
                    return@playerCallback
                }
                val protectionViewer = ProtectionUser.getProtectionUser(viewer)
                val protectionRegion = ProtectionRegion(protectionViewer, region)

                if (ProtectionUtils.standsInProtectedRegion(viewer, region)) {
                    if (protectionViewer.startRegionCreation(protectionRegion, true)) {
                        protectionRegion.setExpandingMarkers()
                        viewer.clearDialogs()
                    }
                } else {
                    viewer.showDialog(createNotOnProtectedRegionNotice())
                }
            }
        }
    }

    private fun createNoButton(regionInfo: RegionInfo, target: OfflinePlayer) = actionButton {
        label { error("Nein") }
        action {
            playerCallback { viewer ->
                viewer.showDialog(
                    ProtectionInfoDialog.createProtectionInfoDialog(
                        viewer,
                        regionInfo,
                        target
                    )
                )
            }
        }
    }

    private fun createCannotExpandNotice() =
        noticeDialogWithBuilder(text("Protection — Erweitern")) {
            error("Das Grundstück darf nicht erweitert werden.")
        }

    private fun createNotOnProtectedRegionNotice() =
        noticeDialogWithBuilder(text("Protection — Erweitern")) {
            error("Du befindest dich nicht auf dem zu erweiternden Grundstück.")
        }
}