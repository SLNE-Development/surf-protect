@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.protect.paper.menu.view.member.ProtectionMemberView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.util.toLocalPlayer
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
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
            externalTitle { text("Mitglied hinzufügen") }
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
                        viewer.sendText {
                            appendErrorPrefix()
                            error("Der Name '")
                            variableValue(memberName)
                            error("' ist ungültig. Er muss zwischen 2 und 16 Zeichen lang sein und darf nur Buchstaben, Zahlen und Unterstriche enthalten.")
                        }
                        viewer.showDialog(addMemberDialog(target, info, memberName.trim()))
                        return@customClick
                    }
                    plugin.launch {
                        val validatedUuid = PlayerLookupService.getUuid(memberName)
                        if (validatedUuid == null) {
                            viewer.sendText {
                                appendErrorPrefix()
                                error("Der Spieler '")
                                variableValue(memberName)
                                error("' wurde nicht gefunden. Bitte überprüfe, dass der Name korrekt ist und versuche es erneut.")
                            }
                            viewer.showDialog(addMemberDialog(target, info, memberName.trim()))
                            return@launch
                        }
                        val offlinePlayer = server.getOfflinePlayer(validatedUuid)
                        withContext(Dispatchers.IO) {
                            offlinePlayer.playerProfile.complete()
                        }

                        if (!offlinePlayer.hasPlayedBefore()) {
                            viewer.sendText {
                                appendErrorPrefix()
                                error("Der Spieler '")
                                variableValue(memberName)
                                error("' hat noch nie auf diesem Server gespielt. Bitte füge den Spieler hinzu, nachdem er mindestens einmal online war.")
                            }
                            viewer.showDialog(addMemberDialog(target, info, memberName.trim()))
                            return@launch
                        }

                        val memberPlayer = memberName.toLocalPlayer()
                        info.region.members.addPlayer(memberPlayer)
                        ProtectionVisualizerManager.onRegionMemberChange(info.region)

                        viewer.sendText {
                            appendSuccessPrefix()
                            success("Das Mitglied '")
                            variableValue(memberName)
                            success("' wurde erfolgreich zur Protection hinzugefügt.")
                        }
                        plugin.launch {
                            viewFrame.open(
                                ProtectionMemberView::class.java,
                                viewer,
                                ImmutableMap.of("region-info", info)
                            )
                        }
                    }
                }
            }
        }

    private fun backButton(info: RegionInfo, target: OfflinePlayer): ActionButton = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { player ->
                plugin.launch {
                    viewFrame.open(
                        ProtectionMemberView::class.java,
                        player,
                        ImmutableMap.of("region-info", info)
                    )
                }
            }
        }
    }
}