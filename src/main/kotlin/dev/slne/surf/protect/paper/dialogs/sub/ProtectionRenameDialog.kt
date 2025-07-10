@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
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
import dev.slne.transaction.api.TransactionApi
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
                    variableValue("${ProtectionSettings.PROTECTION_RENAME_PRICE} ${ProtectionSettings.CURRENCY_NAME}")
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
                    viewer.showDialog(createInvalidNameNotice(info, target))
                    return@customPlayerClick
                }
                val region = info.region
                if (newName == info.name) {
                    viewer.showDialog(createNameIsSameNotice(info, target))
                    return@customPlayerClick
                }

                val cost = ProtectionSettings.PROTECTION_RENAME_PRICE.toBigDecimal()
                val currency =
                    TransactionApi.getCurrency(ProtectionSettings.CURRENCY_NAME).orElseThrow()
                val protectionViewer = ProtectionUser.getProtectionUser(viewer)

                plugin.launch {
                    val hasEnoughMoney = protectionViewer.hasEnoughCurrency(cost, currency)
                    if (!hasEnoughMoney) {
                        viewer.showDialog(createInsufficientFundsNotice(info, target))
                        return@launch
                    }
                    val previousName = info.name
                    val transactionResult = protectionViewer.addTransaction(
                        null, cost.negate(), currency,
                        ProtectionRenameData(region, previousName, newName)
                    )
                    if (transactionResult != TransactionAddResult.SUCCESS) {
                        viewer.showDialog(createInsufficientFundsNotice(info, target))
                        return@launch
                    }
                    info.setProtectionInfoToRegion(ProtectionFlagInfo(newName))
                    viewer.showDialog(createProtectionRenamedNotice(previousName, newName, info, target))
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

    private fun createInvalidNameNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { error("Ungültiger Name") }
            body {
                plainMessage {
                    error("Der eingegebene Name ist ungültig.")
                    error("Der Name darf keine Leerzeichen enthalten, muss mindestens 3 Zeichen lang sein und darf nur Buchstaben, Zahlen und Unterstriche enthalten.")
                }
            }
            afterAction(DialogBase.DialogAfterAction.NONE)
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback { viewer ->
                        viewer.showDialog(createProtectionRenameDialog(info, target))
                    }
                }
            }
        }
    }

    private fun createNameIsSameNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { error("Ungültiger Name") }
            body {
                plainMessage {
                    error("Der eingegebene Name ist derselbe wie der aktuelle Name. ")
                    error("Bitte gebe einen anderen Namen ein.")
                }
            }
            afterAction(DialogBase.DialogAfterAction.NONE)
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback { it.showDialog(createProtectionRenameDialog(info, target)) }
                }
            }
        }
    }

    private fun createInsufficientFundsNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { error("Zu teuer!") }
            body {
                plainMessage {
                    error("Du hast nicht genügend Geld um dieses Grundstück umzubenennen.")
                }
            }
            afterAction(DialogBase.DialogAfterAction.NONE)
        }
        type {
            notice {
                label { text("Zurück") }
                action {
                    callback { it.showDialog(createProtectionRenameDialog(info, target)) }
                }
            }
        }
    }

    private fun createProtectionRenamedNotice(oldName: String, newName: String, info: RegionInfo, target: OfflinePlayer) =
        dialog {
            base {
                title { primary("Protection — Grundstück umbenannt") }
                afterAction(DialogBase.DialogAfterAction.NONE)
                body {
                    plainMessage {
                        success("Du hast das Grundstück ")
                        variableValue(oldName)
                        success(" erfolgreich in ")
                        variableValue(newName)
                        success(" umbenannt.")
                    }
                }
            }
            type {
                notice {
                    label { text("Zurück") }
                    action {
                        playerCallback { viewer ->
                            viewer.showDialog(
                                ProtectionInfoDialog.createProtectionInfoDialog(viewer, info, target)
                            )
                        }
                    }
                }
            }
        }
}