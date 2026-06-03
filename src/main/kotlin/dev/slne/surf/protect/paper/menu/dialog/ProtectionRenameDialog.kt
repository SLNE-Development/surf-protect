package dev.slne.surf.protect.paper.menu.dialog

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.util.toCharSet
import dev.slne.surf.api.paper.dialog.*
import dev.slne.surf.api.paper.nms.NmsUseWithCaution
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.ProtectionFlagInfo
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.transactionUser
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

@OptIn(NmsUseWithCaution::class)
@Suppress("UnstableApiUsage")
fun protectionRenameDialog(protection: RegionInfo, afterRename: () -> Unit) = dialog {
    base {
        afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
        title {
            protectColored("Grundstück umbenennen...")
        }
        body {
            plainMessage {
                protectColored("Gib den neuen Namen für dein Grundstück ein.")
                appendNewline()
                appendNewline {
                    warning("Dieser Vorgang kostet dich ")
                    append(config.currency.currency.format(config.protection.renamePrice.toDouble()))
                    warning("!")
                }
                appendNewline {
                    warning("Der Name darf maximal 22 Zeichen lang sein!")
                }
            }
            input {
                text("new_name") {
                    label { text("Neuer Name") }
                    maxLength(22)
                    initial(protection.name)
                }
            }
        }
    }

    type {
        confirmation {
            no {
                label { error("Abbrechen") }
                action {
                    playerCallback { it.clearDialogs(true) }
                }
            }
            yes {
                label { success("Umbenennen") }
                action {
                    customPlayerClick { context, player ->
                        val newName = context.getText("new_name") ?: ""
                        handleRename(player, newName, protection, afterRename)
                    }
                }
            }
        }
    }
}

private val chars = (('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ', '_', '-')).toCharSet()

@Suppress("UnstableApiUsage")
@OptIn(NmsUseWithCaution::class)
private fun handleRename(player: Player, newName: String, protection: RegionInfo, afterRename: () -> Unit) {
    if (newName.length > 22) {
        player.showDialog(
            noticeDialog(
                title = text("Ungültiger Name", Colors.ERROR),
                notice = text("Der Name darf maximal 22 Zeichen lang sein!", Colors.ERROR)
            )
        )
        return
    }

    if (protection.name == newName) {
        player.showDialog(
            noticeDialog(
                title = text("Ungültiger Name", Colors.ERROR),
                notice = text("Der neue Name ist identisch mit dem alten Name!", Colors.ERROR)
            )
        )
        return
    }

    if (newName.any { !chars.contains(it) }) {
        player.showDialog(
            noticeDialog(
                title = text("Ungültiger Name", Colors.ERROR),
                notice = text(
                    "Der Name darf nur aus Buchstaben, Zahlen, Leerzeichen, Unterstrichen und Bindestrichen bestehen!",
                    Colors.ERROR
                )
            )
        )
        return
    }

    plugin.launch {
        val transactionUser = player.transactionUser()

        val result = transactionUser.withdraw(
            config.protection.renamePrice.toBigDecimal(),
            Currency.default()
        )

        when (result) {
            is TransactionResult.DatabaseError -> {
                player.showDialog(
                    noticeDialog(
                        title = text("Unbekannter Fehler", Colors.ERROR),
                        notice = text(
                            "Es ist ein Fehler bei der Transaktion aufgetreten. Bitte versuche es später erneut.",
                            Colors.ERROR
                        )
                    )
                )
            }

            is TransactionResult.ReceiverInsufficientFunds, TransactionResult.SenderInsufficientFunds -> {
                player.showDialog(
                    noticeDialog(
                        title = text("Nicht genügend Geld", Colors.ERROR),
                        notice = text(
                            "Du hast nicht genügend Geld, um diesen Vorgang durchzuführen!",
                            Colors.ERROR
                        )
                    )
                )
            }

            is TransactionResult.Success -> {
                protection.setProtectionInfoToRegion(ProtectionFlagInfo(newName))

                player.showDialog(
                    noticeDialog(
                        title = text("Erfolgreich", Colors.SUCCESS),
                        notice = text("Du hast dein Grundstück erfolgreich umbenannt!", Colors.SUCCESS)
                    )
                )

                withContext(plugin.entityDispatcher(player)) {
                    afterRename()
                }
            }

            is TransactionResult.TransferSuccess -> {
                player.showDialog(
                    noticeDialog(
                        title = text("???", Colors.ERROR),
                        notice = text(
                            "Diese Nachricht solltest du nicht sehen... Bitte melde diesen Fehler einem Teammitglied!",
                            Colors.ERROR
                        )
                    )
                )
            }
        }
    }
}