@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import dev.slne.surf.protect.paper.dialogs.ProtectionMainDialog
import dev.slne.surf.protect.paper.settings.ProtectionUserSettings
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.Colors
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import net.kyori.adventure.text.format.TextColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

object ProtectionSettingsDialog {
    fun settingsDialog(viewer: Player, target: OfflinePlayer): Dialog = dialog {
        base {
            title { primary("Protection — Einstellungen") }
            preventClosingWithEscape()
            afterAction(DialogBase.DialogAfterAction.NONE)
            input {
                for (setting in ProtectionUserSettings.entries) {
                    simpleBoolean(
                        setting.ordinal.toString(),
                        setting.displayName,
                        setting.getValue(viewer)
                    )
                }
            }
        }

        type {
            confirmation(createSaveButton(target), createBackButton(target, Colors.ERROR))
        }
    }

    private fun createSaveButton(target: OfflinePlayer) = actionButton {
        label { success("Speichern") }
        action {
            customPlayerClick { response, viewer ->
                for (setting in ProtectionUserSettings.entries) {
                    val newValue = response.getBoolean(setting.ordinal.toString()) ?: continue
                    setting.setValue(viewer, newValue)
                }
                viewer.showDialog(createSettingsSavedNotice(target))
            }
        }
    }

    private fun createSettingsSavedNotice(target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Einstellungen gespeichert") }
            afterAction(DialogBase.DialogAfterAction.NONE)
            body {
                plainMessage {
                    success("Die Einstellungen wurden erfolgreich gespeichert.")
                }
            }
        }
        type {
            notice(createBackButton(target))
        }
    }

    private fun createBackButton(target: OfflinePlayer, color: TextColor? = null) = actionButton {
        label { text("Zurück", color) }
        action {
            playerCallback { it.showDialog(ProtectionMainDialog.mainDialog(it, target)) }
        }
    }
}