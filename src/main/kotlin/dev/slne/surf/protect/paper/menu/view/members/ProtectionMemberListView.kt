package dev.slne.surf.protect.paper.menu.view.members

import com.sk89q.worldedit.bukkit.BukkitAdapter
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemType
import org.bukkit.inventory.meta.SkullMeta

@Suppress("UnstableApiUsage")
object ProtectionMemberListView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")

    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            protectionState.get(context).members
        }.elementFactory { _, builder, _, player ->
            builder.withItem(buildItem(Material.PLAYER_HEAD) {
                displayName {
                    protectColored(player.displayName)
                }

                editMeta(SkullMeta::class.java) {
                    it.owningPlayer = BukkitAdapter.adapt(player)
                }
            }).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(RemoveMemberConfigView::class.java)
            }
        }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Mitglieder".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOO?OOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "OOPOBONOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('B', backItem).onClick { context ->
            context.openForPlayer(
                ProtectionInfoView::class.java,
                mapOf("protection" to protectionState.get(context))
            )
        }

        render.layoutSlot('O', outlineItem)

        render
            .layoutSlot('P')
            .updateOnStateChange(paginationState)
            .displayIf { _ ->
                pagination.canBack()
            }
            .onRender { slotRender ->
                if (pagination.canBack()) {
                    slotRender.item = previousItem
                } else {
                    slotRender.item = outlineItem
                }
            }
            .onClick { context ->
                pagination.back()
                pagination.update()
                context.playNewPageSound()
            }

        render
            .layoutSlot('N')
            .updateOnStateChange(paginationState)
            .displayIf { _ ->
                pagination.canAdvance()
            }
            .onRender { slotRender ->
                if (pagination.canAdvance()) {
                    slotRender.item = nextItem
                } else {
                    slotRender.item = outlineItem
                }
            }
            .onClick { context ->
                pagination.advance()
                pagination.update()
                context.playNewPageSound()
            }
    }
}

@Suppress("UnstableApiUsage")
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

fun SlotClickContext.playGeneralClickSound() {
    player.playSound(true) {
        type(Sound.UI_BUTTON_CLICK)
    }
}

fun SlotClickContext.playNewPageSound() {
    player.playSound(true) {
        type(Sound.ENTITY_CHICKEN_EGG)
    }
}