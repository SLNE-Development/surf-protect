package dev.slne.surf.protect.paper.menu.dialog

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.Bukkit


@Suppress("UnstableApiUsage")
fun protectionAddMemberDialog(protection: RegionInfo, afterAdd: () -> Unit) = dialog {
    base {
        afterAction(DialogBase.DialogAfterAction.CLOSE)
        title {
            protectColored("Spieler hinzufügen...")
        }

        body {
            plainMessage {
                protectColored("Gib den Namen des Spielers ein, den du hinzufügen möchtest.")
                appendNewline()
                error("Der Spieler muss bereits einmal auf diesem Server gespielt haben, damit er hinzugefügt werden kann.")
            }
        }

        input {
            text("player_name") {
                label { text("Spielername") }
                maxLength(16)
            }
        }
    }

    type {
        confirmation {
            no {
                label { error("Abbrechen") }
            }
            yes {
                label { success("Hinzufügen") }
                action {
                    customClick { context, audience ->
                        val playerName = context.getText("player_name") ?: ""

                        if (playerName.isEmpty() || playerName.isBlank() || playerName.length > 16) {
                            audience.sendText {
                                appendErrorPrefix()
                                error("Der Spielername ist ungültig.")
                            }
                            return@customClick
                        }

                        val offlinePlayer = Bukkit.getOfflinePlayer(playerName)
                        if (!offlinePlayer.hasPlayedBefore()) {
                            audience.sendText {
                                appendErrorPrefix()
                                error("Der Spieler wurde nicht gefunden.")
                            }
                            return@customClick
                        }

                        if (protection.region.members.contains(offlinePlayer.uniqueId)) {
                            audience.sendText {
                                appendErrorPrefix()
                                error("Der Spieler ist bereits Mitglied dieses Grundstücks.")
                            }
                            return@customClick
                        }

                        protection.region.members.addPlayer(offlinePlayer.uniqueId)
                        ProtectionVisualizerManager.onRegionMemberChange(protection.region)

                        audience.sendText {
                            appendSuccessPrefix()
                            success("Der Spieler wurde erfolgreich als Mitglied hinzugefügt.")
                        }
                        afterAdd()
                    }
                }
            }
        }
    }
}