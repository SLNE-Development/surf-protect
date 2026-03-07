package dev.slne.surf.protect.paper.menu.view.list

import dev.slne.surf.parkour.paper.menu.util.*
import dev.slne.surf.parkour.paper.service.playerTextureService
import dev.slne.surf.parkour.paper.util.formatMillis
import dev.slne.surf.parkour.paper.util.playerName
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionMainView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.allRegions
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
import org.bukkit.Sound
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionListView : View() {
    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            context.player.protectionUser().localPlayer.allRegions()
                .map { RegionInfo(it) }.toMutableList()
        }.elementFactory { _, builder, _, stats ->
            builder.withItem(createRegionItem(stats)).onClick { context ->
                context.playGeneralClickSound()
            }
        }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Deine Grundstücke".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "OAOPBNOOS"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('B', backItem).onClick { context ->
            context.openForPlayer(ProtectionMainView::class.java)
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
fun createRegionItem(stats: RegionInfo) = ItemType.DIRT.createItemStack().apply {
    displayName {
        protectColored(playerTextureService.getTexture(stats.playerUuid).playerName)
    }

    buildLore {
        emptyLine()
        line {
            parkourColored("Parkourstatistiken".toSmallCaps(), TextDecoration.BOLD)
        }
        line {
            spacer("-")
            appendSpace()
            parkourColored("Highscore: ")
            variableValue(stats.highscore)
        }
        line {
            spacer("-")
            appendSpace()
            parkourColored("Versuche: ")
            variableValue(stats.totalRuns)
        }
        line {
            spacer("-")
            appendSpace()
            parkourColored("Gesamtsprünge: ")
            variableValue(stats.totalJumps)
        }
        line {
            spacer("-")
            appendSpace()
            parkourColored("Durchschnittliche Zeit: ")
            variableValue(formatMillis(stats.averageTime))
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