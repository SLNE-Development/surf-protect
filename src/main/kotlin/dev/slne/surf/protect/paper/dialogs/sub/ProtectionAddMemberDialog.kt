@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.util.toLocalPlayer
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.OfflinePlayer

object ProtectionAddMemberDialog {
    private val namePattern = "^[a-zA-Z0-9_]{2,16}$".toRegex()

    fun addMemberDialog(target: OfflinePlayer, info: RegionInfo, initial: String? = null) = dialog {
        base {
            title { primary("Protection — Mitglied hinzufügen") }
            afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            body {
                plainMessage(400) {
                    info("Hier kannst du ein neues Mitglied zu deiner Protection hinzufügen.")
                }
            }
            input {
                text("member_name") {
                    label { text("Name des Mitglieds") }
                    maxLength(16)
                    this.initial = initial
                }
            }
        }
        type {
            confirmation(addMemberButton(target, info), backButton(info, target))
        }
    }

    private fun addMemberButton(target: OfflinePlayer, info: RegionInfo): ActionButton =
        actionButton {
            label { success("Hinzufügen") }
            action {
                customClick { response, viewer ->
                    val memberName = response.getText("member_name") ?: return@customClick
                    if (!namePattern.matches(memberName)) {
                        viewer.showDialog(createInvalidMemberNameNotice(target, info, memberName))
                        return@customClick
                    }
                    plugin.launch {
                        val validatedUuid = PlayerLookupService.getUuid(memberName)
                        if (validatedUuid == null) {
                            viewer.showDialog(createNoPlayerFoundNotice(target, info, memberName))
                            return@launch
                        }
                        val offlinePlayer = server.getOfflinePlayer(validatedUuid)
                        withContext(Dispatchers.IO) {
                            offlinePlayer.playerProfile.complete()
                        }

                        if (!offlinePlayer.hasPlayedBefore()) {
                            viewer.showDialog(
                                createPlayerNotPlayedBeforeNotice(
                                    target,
                                    info,
                                    memberName
                                )
                            )
                            return@launch
                        }

                        val memberPlayer = memberName.toLocalPlayer()
                        info.region.members.addPlayer(memberPlayer)
                        ProtectionVisualizerManager.updateVisualizer(info.region)

                        viewer.showDialog(
                            createMemberAddedNotice(target, info, memberName)
                        )
                    }
                }
            }
        }

    private fun createInvalidMemberNameNotice(
        target: OfflinePlayer,
        info: RegionInfo,
        input: String
    ) = dialog {
        base {
            title { text("Protection — Ungültiger Name") }
            body {
                plainMessage {
                    error("Der Name '")
                    variableValue(input)
                    error("' ist ungültig. Er muss zwischen 2 und 16 Zeichen lang sein und darf nur Buchstaben, Zahlen und Unterstriche enthalten.")
                }
            }
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback {
                        it.showDialog(addMemberDialog(target, info, input.trim()))
                    }
                }
            }
        }
    }

    private fun createNoPlayerFoundNotice(
        target: OfflinePlayer,
        info: RegionInfo,
        input: String
    ) = dialog {
        base {
            title { text("Protection — Spieler nicht gefunden") }
            body {
                plainMessage {
                    error("Der Spieler '")
                    variableValue(input)
                    error("' wurde nicht gefunden. Bitte überprüfe, dass der Name korrekt ist und versuche es erneut.")
                }
            }
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback {
                        it.showDialog(addMemberDialog(target, info, input.trim()))
                    }
                }
            }
        }
    }

    private fun createPlayerNotPlayedBeforeNotice(
        target: OfflinePlayer,
        info: RegionInfo,
        input: String
    ) = dialog {
        base {
            title { text("Protection — Spieler hat noch nicht gespielt") }
            body {
                plainMessage {
                    error("Der Spieler '")
                    variableValue(input)
                    error("' hat noch nie auf diesem Server gespielt. Bitte füge den Spieler hinzu, nachdem er mindestens einmal online war.")
                }
            }
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback {
                        it.showDialog(addMemberDialog(target, info, input.trim()))
                    }
                }
            }
        }
    }

    private fun createMemberAddedNotice(
        target: OfflinePlayer,
        info: RegionInfo,
        memberName: String
    ) = dialog {
        base {
            title { text("Protection — Mitglied hinzugefügt") }
            body {
                plainMessage {
                    success("Das Mitglied '")
                    variableValue(memberName)
                    success("' wurde erfolgreich zur Protection hinzugefügt.")
                }
            }
        }
        type {
            notice(backButton(info, target))
        }
    }

    private fun backButton(info: RegionInfo, target: OfflinePlayer): ActionButton = actionButton {
        label { text("Zurück") }
        action {
            callback {
                it.showDialog(ProtectionMemberDialog.createProtectionMemberDialog(target, info))
            }
        }
    }
}