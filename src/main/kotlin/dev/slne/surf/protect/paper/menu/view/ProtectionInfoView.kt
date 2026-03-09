package dev.slne.surf.protect.paper.menu.view

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.menu.dialog.protectionRenameDialog
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.flags.ProtectionEditFlagsView
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.menu.view.members.ProtectionMemberListView
import dev.slne.surf.protect.paper.menu.view.sell.ProtectionSellConfirmView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.standsInProtectedRegion
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionInfoView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Deine Gründstücke - Info".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O       O",
                "ORM I SFO",
                "O   E   O",
                "OOOOBOOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(ProtectionListView::class.java)
        }
        render.layoutSlot('M', membersItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionMemberListView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('I', createRegionItem(protectionState.get(render)))
        render.layoutSlot('S', sellItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionSellConfirmView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('R', renameItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            click.player.showDialog(protectionRenameDialog(protectionState.get(click)))
        }
        render.layoutSlot('F', editFlags).onClick { onClick ->
            onClick.playGeneralClickSound()
            onClick.openForPlayer(
                ProtectionEditFlagsView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('E', expandItem).onClick { click ->
            click.playGeneralClickSound()

            val protectionUser = click.player.protectionUser()
            val protection = protectionState.get(render)

            val protectionRegion = ProtectionRegion(
                protectionUser,
                click.player,
                click.player.inventory.contents,
                protection.region
            )

            plugin.launch {
                if (click.player.standsInProtectedRegion(protection.region)) {
                    val started =
                        click.player.protectionUser().startRegionCreation(protectionRegion)

                    if (started) {
                        withContext(plugin.entityDispatcher(click.player)) {
                            click.closeForPlayer()
                        }

                        protectionRegion.setCornerMarkers()
                        protectionUser.updateMarkerItems()
                    } else {
                        click.playNoSound()
                    }
                } else {
                    click.playNoSound()
                    click.player.sendText {
                        appendErrorPrefix()
                        error("Du musst dich auf deinem Grundstück befinden, um es erweitern zu können.")
                    }
                    withContext(plugin.entityDispatcher(click.player)) {
                        click.closeForPlayer()
                    }
                }
            }
        }
    }

    private val membersItem = ItemType.PLAYER_HEAD.createItemStack().apply {
        displayName {
            protectColored("Mitglieder")
        }
    }

    private val sellItem = ItemType.EMERALD.createItemStack().apply {
        displayName {
            protectColored("Verkaufen")
        }
    }

    private val renameItem = ItemType.NAME_TAG.createItemStack().apply {
        displayName {
            protectColored("Umbenennen")
        }
    }

    private val expandItem = ItemType.OAK_SIGN.createItemStack().apply {
        displayName {
            protectColored("Erweitern")
        }
    }

    private val editFlags = ItemType.REDSTONE_TORCH.createItemStack().apply {
        displayName {
            protectColored("Flags bearbeiten")
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
}

