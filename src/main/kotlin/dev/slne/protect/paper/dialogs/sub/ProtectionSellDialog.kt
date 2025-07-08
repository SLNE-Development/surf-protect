@file:Suppress("UnstableApiUsage")

package dev.slne.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.protect.paper.bukkitInstance
import dev.slne.protect.paper.plugin
import dev.slne.protect.paper.region.ProtectionUtils
import dev.slne.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.protect.paper.region.info.RegionInfo
import dev.slne.protect.paper.region.settings.ProtectionSettings
import dev.slne.protect.paper.region.transaction.ProtectionSellData
import dev.slne.protect.paper.user.ProtectionUser
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialogWithBuilder
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.transaction.api.TransactionApi
import dev.slne.transaction.api.currency.Currency
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.text.DecimalFormat

object ProtectionSellDialog {
    fun createSellDialog(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Grundstück verkaufen") }
            afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            body {
                plainMessage {
                    error("Bist du dir sicher, dass du das Grundstück verkaufen möchtest?")
                }
                plainMessage {
                    error("Achtung: Das Grundstück kann nicht wiederhergestellt werden!")
                }
                plainMessage {
                    error("Für das Grundstück wird dir ein Anteil des Kaufpreises erstattet.")
                }
            }
        }
        type {
            confirmation(createSellButton(info, target), createBackButton(info, target))
        }
    }

    private fun createSellButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { success("Verkaufen") }
        action {
            playerCallback { viewer ->
                val protectionViewer = ProtectionUser.getProtectionUser(viewer)
                val region = info.region
                val canSellState = region.getFlag(ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG)
                val canSell = canSellState == StateFlag.State.ALLOW || canSellState == null

                if (!canSell) {
                    viewer.showDialog(createCannotSellNotice(info, target))
                    return@playerCallback
                }

                if (isRegionEdited(region)) {
                    viewer.showDialog(createPlotIsEditedNotice(info, target))
                    return@playerCallback
                }

                val refund = info.retailPrice.toBigDecimal()
                val currency =
                    TransactionApi.getCurrency(ProtectionSettings.CURRENCY_NAME).orElseThrow()
                val regionManager = ProtectionUtils.getRegionManager(info.world)
                if (!regionManager.hasRegion(region.id)) {
                    viewer.showDialog(createPlotDoesNotExistNotice())
                    return@playerCallback
                }

                buildList {
                    addAll(region.owners.playerDomain.uniqueIds)
                    addAll(region.members.playerDomain.uniqueIds)
                }.mapNotNull { Bukkit.getPlayer(it) }
                    .forEach { notifyDeletion(it, info) }

                regionManager.removeRegion(region.id)
                val visualizerThread = bukkitInstance.protectionVisualizerThread
                visualizerThread.visualizers
                    .find { it.region == region }
                    ?.let { visualizerThread.closeVisualizer(it) }

                plugin.launch {
                    protectionViewer.addTransaction(
                        null,
                        refund,
                        currency,
                        ProtectionSellData(info.world, region)
                    ).await()
                    viewer.showDialog(createPlotSoldNotice(refund, currency))
                }
            }
        }
    }

    private fun createCannotSellNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Grundstück verkaufen") }
            body {
                plainMessage {
                    error("Du kannst dieses Grundstück nicht verkaufen, da es nicht zum Verkauf freigegeben ist.")
                }
            }
        }
        type {
            notice(createBackButton(info, target))
        }
    }

    private fun createPlotIsEditedNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Grundstück verkaufen") }
            body {
                plainMessage {
                    error("Das Grundstück wird gerade bearbeitet!")
                }
            }
        }
        type {
            notice(createBackButton(info, target))
        }
    }

    private fun createBackButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { viewer ->
                viewer.showDialog(
                    ProtectionInfoDialog.createProtectionInfoDialog(
                        viewer,
                        info,
                        target
                    )
                )
            }
        }
    }

    private fun createPlotDoesNotExistNotice() =
        noticeDialogWithBuilder(
            text(
                "Protection — Grundstück verkaufen",
                Colors.Companion.PRIMARY
            )
        ) {
            error("Das Grundstück existiert nicht mehr!")
        }

    private fun createPlotSoldNotice(amount: BigDecimal, currency: Currency) =
        noticeDialogWithBuilder(text("Grundstück verkauft", Colors.Companion.SUCCESS)) {
            info("Du hast dein Grundstück für ")
            variableValue(DecimalFormat.getNumberInstance().format(amount))
            appendSpace()
            append(currency.displayName)
            info(" verkauft.")
        }

    private fun isRegionEdited(region: ProtectedRegion): Boolean {
        return bukkitInstance.userManager.users.any { user ->
            user.hasRegionCreation() &&
                    user.regionCreation.expandingProtection?.id == region.id
        }
    }

    private fun notifyDeletion(player: Player, regionInfo: RegionInfo) {
        player.sendText {
            appendPrefix()
            info("Das Grundstück ")
            variableValue(regionInfo.name)
            info(" wurde verkauft.")
        }
    }
}