@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.transaction.api.currency.Currency
import io.papermc.paper.registry.data.dialog.DialogBase
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
                plainMessage(400) {
                    error("Bist du dir sicher, dass du das Grundstück verkaufen möchtest?")
                    appendNewline()
                    error("Achtung: Das Grundstück kann nicht wiederhergestellt werden!")
                    appendNewline()
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
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Du kannst dieses Grundstück nicht verkaufen, da es nicht zum Verkauf freigegeben ist.")
                    }
                    viewer.showDialog(ProtectionInfoDialog.createProtectionInfoDialog(viewer, info, target))
                    return@playerCallback
                }

                if (isRegionEdited(region)) {
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Das Grundstück wird gerade bearbeitet!")
                    }
                    viewer.showDialog(ProtectionInfoDialog.createProtectionInfoDialog(viewer, info, target))
                    return@playerCallback
                }

                val refund = info.retailPrice.toBigDecimal()
                val currency = config.currency.currency
                val regionManager = info.regionManager
                if (regionManager == null) {
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Das Grundstück existiert nicht mehr!")
                    }
                    viewer.clearDialogs()
                    return@playerCallback
                }

                buildList {
                    addAll(region.owners.playerDomain.uniqueIds)
                    addAll(region.members.playerDomain.uniqueIds)
                }.mapNotNull { Bukkit.getPlayer(it) }
                    .forEach { notifyDeletion(it, info) }

                regionManager.removeRegion(region.id)
                ProtectionVisualizerManager.onRegionDeletion(region)

                plugin.launch {
                    protectionViewer.transactionUser.deposit(refund, currency)
                    viewer.sendText {
                        appendSuccessPrefix()
                        success("Du hast dein Grundstück für ")
                        variableValue(DecimalFormat.getNumberInstance().format(refund))
                        appendSpace()
                        append(currency.displayName)
                        success(" verkauft.")
                    }
                    viewer.clearDialogs()
                }
            }
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

    private fun isRegionEdited(region: ProtectedRegion): Boolean {
        return ProtectionUserManager.all()
            .any { it.regionCreation?.expandingProtection?.id == region.id }
    }

    private fun notifyDeletion(player: Player, regionInfo: RegionInfo) {
        player.sendText {
            appendInfoPrefix()
            info("Das Grundstück ")
            variableValue(regionInfo.name)
            info(" wurde verkauft.")
        }
    }
}