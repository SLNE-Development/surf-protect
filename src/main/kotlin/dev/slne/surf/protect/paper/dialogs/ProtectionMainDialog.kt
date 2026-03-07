@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs

import dev.slne.surf.protect.paper.dialogs.sub.ProtectionCreateDialog
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionListDialog
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object ProtectionMainDialog {
    fun mainDialog(viewer: Player, target: OfflinePlayer): Dialog = dialog {
        base {
            title { primary("Protections — Menü") }
            afterAction(DialogBase.DialogAfterAction.NONE)
        }
        type {
            multiAction {
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_LIST_PERMISSION)) {
                    action(listProtectionsButton(target))
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_VISUALIZE_PERMISSION)) {
                    action(visualizeProtectionsButton())
                }
                if (viewer.hasPermission(ProtectPermissionRegistry.PROTECTION_CREATE_PERMISSION)) {
                    action(createProtectionButton(target))
                }
            }
        }
    }

    private fun listProtectionsButton(target: OfflinePlayer) = actionButton {
        label { text("Meine Grundstücke") }
        tooltip { info("Eine Liste mit allen deinen Grundstücken") }
        action {
            playerCallback { it.showDialog(ProtectionListDialog.listProtectionsDialog(it, target)) }
        }
    }

    private fun visualizeProtectionsButton() = actionButton {
        label { text("Visualisieren") }
        tooltip { info("Aktiviert/Deaktiviert den Visualizer") }
        action {
            playerCallback { viewer ->
                val state = ProtectionVisualizerManager.switchVisualizing(viewer)
                viewer.sendText {
                    appendInfoPrefix()
                    info("Du hast die Visualisierung der Grundstücke ")
                    if (state) success("aktiviert") else error("deaktiviert")
                    info(". Bitte warte einen kleinen Moment, bis die Änderungen wirksam werden.")
                }
            }
        }
    }

    private fun createProtectionButton(target: OfflinePlayer) = actionButton {
        label { text("Grundstück erstellen") }
        tooltip { info("Erstelle ein neues Grundstück") }
        action {
            callback { it.showDialog(ProtectionCreateDialog.protectionCreateDialog(target)) }
        }
    }
}