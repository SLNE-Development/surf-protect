package dev.slne.surf.protect.paper.menu.view.sell

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.paper.dialog.*
import dev.slne.surf.api.paper.nms.NmsUseWithCaution
import dev.slne.surf.protect.paper.menu.util.playYesSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.util.renderRegionInformation
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.transaction.api.currency.Currency
import io.papermc.paper.registry.data.dialog.DialogBase
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextDecoration
import kotlin.math.roundToInt

@Suppress("UnstableApiUsage")
@OptIn(NmsUseWithCaution::class)
fun protectionSellConfirmationDialog(regionInfo: RegionInfo, afterSell: () -> Unit) = dialog {
    base {
        afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
        title {
            protectColored("Grundstück — Verkaufen".toSmallCaps(), TextDecoration.BOLD)
        }
        body {
            plainMessage {
                warning("Bist du dir sicher, dass du das Grundstück ")
                variableValue(regionInfo.name)
                warning(" verkaufen willst?")
                appendNewline(2)
                append(Component.join(JoinConfiguration.newlines(), renderRegionInformation(regionInfo, false)))
            }
        }
    }

    type {
        confirmation {
            no {
                label { error("Abbrechen") }
                action {
                    playerCallback {
                        it.clearDialogs(true)
                    }
                }
            }
            yes {
                label { success("Verkaufen") }
                action {
                    playerCallback { player ->
                        val region = regionInfo.region
                        val canSellState = region.getFlag(ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG)
                        val canSell = canSellState == StateFlag.State.ALLOW || canSellState == null

                        if (!canSell) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Dieses Grundstück kann nicht verkauft werden!")
                            }
                            return@playerCallback
                        }

                        val regionManager = regionInfo.regionManager

                        if (regionManager == null) {
                            player.sendText {
                                appendErrorPrefix()
                                error("Dieses Grundstück existiert nicht mehr!")
                            }
                            return@playerCallback
                        }

                        plugin.launch {
                            val result = player.protectionUser()
                                .transactionUser
                                .deposit(
                                    regionInfo.retailPrice.roundToInt().toBigDecimal(),
                                    Currency.default()
                                )

                            if (!result.success) {
                                player.showDialog(
                                    noticeDialog(
                                        text("Ein Fehler ist aufgetreten", Colors.ERROR),
                                        text(
                                            "Bei dem Verkauf des Grundstücks ist ein Fehler aufgetreten!",
                                            Colors.ERROR
                                        )
                                    )
                                )
                                return@launch
                            }

                            regionManager.removeRegion(region.id)
                            ProtectionVisualizerManager.onRegionDeletion(region)

                            withContext(plugin.entityDispatcher(player)) {
                                player.playYesSound()
                                player.clearDialogs(true)
                                afterSell()
                            }
                        }
                    }
                }
            }
        }
    }
}