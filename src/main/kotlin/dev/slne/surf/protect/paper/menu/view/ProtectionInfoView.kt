package dev.slne.surf.protect.paper.menu.view

import dev.slne.surf.protect.paper.menu.util.backItem
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.menu.view.list.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.view.members.ProtectionMemberListView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
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
                "O M I S O",
                "O   R   O",
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
        render.layoutSlot('S', sellItem)
        render.layoutSlot('R', renameItem)
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

