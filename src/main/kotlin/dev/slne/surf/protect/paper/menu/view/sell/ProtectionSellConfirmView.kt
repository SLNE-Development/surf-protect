package dev.slne.surf.protect.paper.menu.view.sell

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionSellConfirmView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Verkaufen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout(
                "OOOOOOOOO",
                "ORCRIRYRO",
                "OOOOOOOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', createRegionItem(protectionState.get(render)))
        render.layoutSlot('C', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionInfoView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('Y', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val protection = protectionState.get(click)

            val region = protection.region
            val canSellState = region.getFlag(ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG)
            val canSell = canSellState == StateFlag.State.ALLOW || canSellState == null

            if (!canSell) {
                click.player.sendText {
                    appendErrorPrefix()
                    error("Dieses Grundstück kann nicht verkauft werden!")
                }
                return@onClick
            }

            val regionManager = protection.regionManager

            if (regionManager == null) {
                click.player.sendText {
                    appendErrorPrefix()
                    error("Dieses Grundstück existiert nicht mehr!")
                }
                return@onClick
            }

            plugin.launch {
                click.player.protectionUser().transactionUser.deposit(
                    protection.retailPrice.toBigDecimal(),
                    Currency.default()
                )

                regionManager.removeRegion(region.id)
                ProtectionVisualizerManager.onRegionDeletion(region)

                withContext(plugin.entityDispatcher(click.player)) {
                    click.openForPlayer(
                        ProtectionInfoView::class.java,
                        mapOf("protection" to protection)
                    )
                }
            }
        }
    }

    fun createRegionItem(protection: RegionInfo) = ItemType.DIRT.createItemStack().apply {
        displayName {
            protectColored(protection.name)
        }

        buildLore {
            emptyLine()
            line {
                protectColored("Grundstücksinformation".toSmallCaps(), TextDecoration.BOLD)
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Besitzer: ")
                variableValue(protection.owners.joinToString(", ") { it.displayName })
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Id: ")
                variableValue(protection.region.id)
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Fläche: ")
                variableValue("${protection.volume} Blöcke")
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Mitglieder: ")
                variableValue(protection.members.size)
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Bezahlter Preis: ")
                variableValue("${protection.price}CC")
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Verkaufspreis: ")
                variableValue("${protection.retailPrice}CC")
            }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("Abbrechen")
        }
    }

    private val confirmItem = buildItem(Material.GREEN_STAINED_GLASS_PANE) {
        displayName {
            success("Bestätigen")
        }
    }
}