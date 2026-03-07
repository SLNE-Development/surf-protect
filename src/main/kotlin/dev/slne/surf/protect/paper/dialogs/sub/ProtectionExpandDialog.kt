@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.standsInProtectedRegion
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
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
                body { // TODO: 08.07.2025 23:58 - more infos like in create dialog
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
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Das Grundstück darf nicht erweitert werden.")
                    }
                    return@playerCallback
                }

                plugin.launch(plugin.entityDispatcher(viewer)) {
                    val protectionViewer = ProtectionUser.getProtectionUser(viewer)
                    val protectionRegion = ProtectionRegion(
                        protectionViewer,
                        viewer,
                        viewer.inventory.contents,
                        region
                    )

                    if (viewer.standsInProtectedRegion(region)) {
                        val started = protectionViewer.startRegionCreation(protectionRegion) { message ->
                            viewer.sendText { appendErrorPrefix(); append(message) }
                        }
                        if (started) {
                            protectionRegion.setCornerMarkers()
                            protectionViewer.updateMarkerItems()
                            viewer.clearDialogs()
                        }
                    } else {
                        viewer.sendText {
                            appendErrorPrefix()
                            error("Du befindest dich nicht auf dem zu erweiternden Grundstück.")
                        }
                    }
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
}