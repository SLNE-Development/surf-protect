@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.util.getMemberNames
import dev.slne.surf.protect.paper.util.getOwnerNames
import dev.slne.surf.protect.paper.util.toBukkitLocation
import dev.slne.surf.protect.paper.util.world
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialogWithBuilder
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.future.await
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object ProtectionInfoDialog {
    fun createProtectionInfoDialog(
        viewer: Player,
        info: RegionInfo,
        target: OfflinePlayer
    ): Dialog = dialog {
        val center = info.centerLocation
        val bukkitCenter = center?.toBukkitLocation()

        base {
            title {
                primary("Protection Info — ${info.name}")
                afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            }
            externalTitle { text(info.name) }
            body {
                plainMessage {
                    variableKey("Größe: ")
                    variableValue("${info.volume} Blöcke")
                }
                plainMessage {
                    variableKey("Entfernung: ")

                    if (bukkitCenter == null || bukkitCenter.world != viewer.world) {
                        variableValue("Unbekannt")
                    } else {
                        variableValue("${viewer.location.distance(bukkitCenter).toLong()} Blöcke")
                    }
                }
                plainMessage {
                    variableKey("Ort: ")
                    if (center == null) {
                        variableValue("Unbekannt")
                    } else {
                        variableValue("${center.blockX}, ${center.blockY}, ${center.blockZ} — ${center.world.name ?: "Unbekannt"}")
                    }
                }
                plainMessage {
                    variableKey("Besitzer: ")
                    val ownerNames = info.region.getOwnerNames()
                    if (ownerNames.isEmpty()) {
                        variableValue("Unbekannt")
                    } else {
                        variableValue(ownerNames.joinToString(", "))
                    }
                }
                plainMessage {
                    variableKey("Mitglieder: ")
                    val memberNames = info.region.getMemberNames()
                    if (memberNames.isEmpty()) {
                        variableValue("Keine Mitglieder")
                    } else {
                        variableValue(memberNames.joinToString(", "))
                    }
                }
            }
        }
        type {
            multiAction {
                if (bukkitCenter != null && viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_TELEPORT)) {
                    action(teleportButton(bukkitCenter))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_EXPAND)) {
                    action(expandButton(info, target))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_RENAME)) {
                    action(renameButton(info, target))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_EDIT_FLAGS)) {
                    action(editFlagsButton(info, target))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_SELL)) {
                    action(sellButton(info, target))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_MEMBER)) {
                    action(membersButton(info, target))
                }

                exitAction(backButton(target))
            }
        }
    }

    private fun teleportButton(center: Location) = actionButton {
        label { text("Teleportieren") }
        tooltip { info("Klicke, um dich zu dieser Protection zu teleportieren.") }
        action {
            playerCallback { viewer ->
                plugin.launch {
                    val world = center.world
                    val chunk = world.getChunkAtAsync(center).await()
                    val highestY = chunk.getChunkSnapshot(true, false, false)
                        .getHighestBlockYAt(center.blockX and 15, center.blockZ and 15)
                    val tpLocation = center.clone().apply {
                        y = highestY + 1.0
                    }
                    viewer.teleportAsync(tpLocation).await()
                    viewer.showDialog(teleportSuccessNotice())
                }
            }
        }
    }

    private fun teleportSuccessNotice() = noticeDialogWithBuilder(
        text("Protection Info — Teleportation", Colors.PRIMARY)
    ) {
        success("Du wurdest erfolgreich zu der Protection teleportiert.")
    }

    private fun expandButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Grundstück erweitern") }
        tooltip { info("Klicke, um dieses Grundstück zu erweitern.") }
        action {
            callback {
                it.showDialog(
                    ProtectionExpandDialog.createProtectionExpandConfirmationDialog(
                        info,
                        target
                    )
                )
            }
        }
    }

    private fun renameButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Grundstück umbenennen") }
        tooltip {
            info("Klicke, um dieses Grundstück umzubenennen.")
            appendNewline(2)
            error("Achtung:")
            warning(" Für diese Aktion wird eine Gebühr in Höhe von ")
            variableValue("${ProtectionSettings.PROTECTION_RENAME_PRICE} ${ProtectionSettings.CURRENCY_NAME}")
            warning(" berechnet.")
        }
        action {
            callback {
                it.showDialog(
                    ProtectionRenameDialog.createProtectionRenameDialog(
                        info,
                        target
                    )
                )
            }
        }
    }

    private fun editFlagsButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Flags bearbeiten") }
        tooltip { info("Klicke, um die Flags dieses Grundstücks zu bearbeiten.") }
        action {
            callback {
                it.showDialog(
                    ProtectionEditFlagsDialog.createEditFlagsDialog(
                        info,
                        target
                    )
                )
            }
        }
    }

    private fun sellButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { error("Grundstück verkaufen") }
        tooltip { info("Klicke, um dieses Grundstück zu verkaufen.") }
        action {
            callback { it.showDialog(ProtectionSellDialog.createSellDialog(info, target)) }
        }
    }

    private fun membersButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Mitglieder") }
        tooltip { info("Klicke, um die Mitglieder dieses Grundstücks zu verwalten.") }
        action {
            playerCallback { viewer ->
                viewer.showDialog(ProtectionMemberDialog.createProtectionMemberDialog(target, info))
            }
        }
    }

    private fun backButton(target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { viewer ->
                viewer.showDialog(ProtectionListDialog.listProtectionsDialog(viewer, target))
            }
        }
    }
}