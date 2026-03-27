package dev.slne.surf.protect.paper.menu.dialog

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.ProtectionFlagInfo
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.appendWarnPrefix
import dev.slne.surf.protect.paper.util.castCoinFormat
import dev.slne.surf.surfapi.bukkit.api.dialog.search.searchDialog
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.transactionUser
import kotlinx.coroutines.withContext
import org.bukkit.entity.Player

@Suppress("UnstableApiUsage")
fun protectionRenameDialog(protection: RegionInfo) = searchDialog(
    title = {
        protectColored("Grundstück umbenennen...")
    },
    searchInput = {
        initialValue = protection.name
    },
    body = {
        plainMessage {
            protectColored("Gib den neuen Namen für dein Grundstück ein.")
            appendNewline()
            appendWarnPrefix()
            error("Dieser Vorgang kostet dich ${castCoinFormat.format(config.protection.renamePrice)}!")

            appendNewline()
            appendWarnPrefix()
            error("Der Name darf maximal 22 Zeichen lang sein!")
        }
    },
    onSearch = { player, query ->
        handleRename(player, query, protection)
    },
    onClose = { player, query ->
        viewFrame.open(
            ProtectionInfoView::class.java,
            player,
            mapOf("protection" to protection)
        )

        player.sendText {
            appendInfoPrefix()
            info("Du hast den Umbenennen-Vorgang abgebrochen.")
        }
    }
)

private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(' ', '_', '-')

private fun handleRename(player: Player, newName: String, protection: RegionInfo) {
    if (newName.length > 22) {
        player.sendText {
            appendErrorPrefix()
            error("Der Name darf maximal 22 Zeichen lang sein!")
        }
        viewFrame.open(
            ProtectionInfoView::class.java,
            player,
            mapOf("protection" to protection)
        )
        return
    }

    if (protection.name == newName) {
        player.sendText {
            appendErrorPrefix()
            error("Der neue Name ist identisch mit dem alten Name!")
        }
        viewFrame.open(
            ProtectionInfoView::class.java,
            player,
            mapOf("protection" to protection)
        )
        return
    }

    if (newName.any { it !in chars }) {
        player.sendText {
            appendErrorPrefix()
            error("Der Name darf nur aus Buchstaben, Zahlen, Leerzeichen, Unterstrichen und Bindestrichen bestehen!")
        }
        viewFrame.open(
            ProtectionInfoView::class.java,
            player,
            mapOf("protection" to protection)
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
                player.sendText {
                    appendErrorPrefix()
                    error("Es ist ein Fehler bei der Transaktion aufgetreten. Bitte versuche es später erneut.")
                }
            }

            is TransactionResult.ReceiverInsufficientFunds -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast nicht genügend Geld, um diesen Vorgang durchzuführen!")
                }
            }

            is TransactionResult.SenderInsufficientFunds -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du hast nicht genügend Geld, um diesen Vorgang durchzuführen!")
                }
            }

            is TransactionResult.Success -> {
                protection.setProtectionInfoToRegion(ProtectionFlagInfo(newName))

                player.sendText {
                    appendSuccessPrefix()
                    success("Du hast dein Grundstück erfolgreich umbenannt!")
                }
            }

            is TransactionResult.TransferSuccess -> {
                player.sendText {
                    appendSuccessPrefix()
                    success("Diese Nachricht solltest du nicht sehen... Bitte melde diesen Fehler einem Teammitglied!")
                }
            }
        }

        player.closeDialog()
        withContext(plugin.entityDispatcher(player)) {
            viewFrame.open(
                ProtectionInfoView::class.java,
                player,
                mapOf("protection" to protection)
            )
        }
    }
}