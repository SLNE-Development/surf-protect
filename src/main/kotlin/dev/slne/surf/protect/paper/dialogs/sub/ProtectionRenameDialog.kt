@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.ProtectionFlagInfo
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.region.transaction.ProtectionRenameData
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.transaction.api.transaction.result.TransactionAddResult
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.future.await
import org.bukkit.OfflinePlayer

object ProtectionRenameDialog {
    private const val MAX_NAME_LENGTH = 16 + ProtectionSettings.RANDOM_NAME_LENGTH
    private val namePattern = "^[a-zA-Z0-9_-]{3,${MAX_NAME_LENGTH}}".toRegex()

    fun createProtectionRenameDialog(info: RegionInfo, target: OfflinePlayer): Dialog = dialog {
        val region = info.region
        base {
            title { primary("Protection — Grundstück umbenennen") }
            body {
                plainMessage {
                    error("Achtung:")
                    appendNewline()
                    info("Für diese Aktion wird eine Gebühr in Höhe von ")
                    variableValue("${config.protection.renamePrice} ")
                    append(config.currency.currency.displayName.colorIfAbsent(Colors.VARIABLE_VALUE))
                    info(" berechnet.")
                }
            }
            input {
                text("new_name") {
                    label { text("Neuer Name") }
                    initial(
                        region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG)?.name ?: region.id
                    )
                    maxLength(MAX_NAME_LENGTH)
                }
            }
            afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
        }
        type {
            confirmation(createRenameButton(info, target), createBackButton(info, target))
        }
    }

    private fun createRenameButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { success("Umbenennen") }
        action {
            customPlayerClick { response, viewer ->
                val newName = response.getText("new_name") ?: return@customPlayerClick
                if (!namePattern.matches(newName)) {
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Der eingegebene Name ist ungültig. Der Name darf keine Leerzeichen enthalten, muss mindestens 3 Zeichen lang sein und darf nur Buchstaben, Zahlen und Unterstriche enthalten.")
                    }
                    viewer.showDialog(createProtectionRenameDialog(info, target))
                    return@customPlayerClick
                }
                val region = info.region
                if (newName == info.name) {
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Der eingegebene Name ist derselbe wie der aktuelle Name. Bitte gebe einen anderen Namen ein.")
                    }
                    viewer.showDialog(createProtectionRenameDialog(info, target))
                    return@customPlayerClick
                }

                val cost = config.protection.renamePrice.toBigDecimal()
                val currency = config.currency.currency
                val protectionViewer = ProtectionUser.getProtectionUser(viewer)

                plugin.launch {
                    val hasEnoughMoney = protectionViewer.hasEnoughCurrency(cost, currency)
                    if (!hasEnoughMoney) {
                        viewer.sendText {
                            appendErrorPrefix()
                            error("Du hast nicht genügend Geld um dieses Grundstück umzubenennen.")
                        }
                        viewer.showDialog(createProtectionRenameDialog(info, target))
                        return@launch
                    }
                    val previousName = info.name
                    val transactionResult = protectionViewer.addTransaction(
                        null, cost.negate(), currency,
                        ProtectionRenameData(region, previousName, newName)
                    )
                    if (transactionResult != TransactionAddResult.SUCCESS) {
                        viewer.sendText {
                            appendErrorPrefix()
                            error("Du hast nicht genügend Geld um dieses Grundstück umzubenennen.")
                        }
                        viewer.showDialog(createProtectionRenameDialog(info, target))
                        return@launch
                    }
                    info.setProtectionInfoToRegion(ProtectionFlagInfo(newName))
                    viewer.sendText {
                        appendSuccessPrefix()
                        success("Du hast das Grundstück ")
                        variableValue(previousName)
                        success(" erfolgreich in ")
                        variableValue(newName)
                        success(" umbenannt.")
                    }
                    viewer.showDialog(ProtectionInfoDialog.createProtectionInfoDialog(viewer, info, target))
                }
            }
        }
    }

    private fun createBackButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { viewer ->
                viewer.showDialog(ProtectionInfoDialog.createProtectionInfoDialog(viewer, info, target))
            }
        }
    }
}
