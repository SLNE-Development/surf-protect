package dev.slne.surf.protect.paper.menu.view.list

import com.google.common.collect.ImmutableMap
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionMainView
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.allRegions
import dev.slne.surf.protect.paper.util.getMemberNames
import dev.slne.surf.protect.paper.util.getOwnerNames
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

@Suppress("UnstableApiUsage")
object ProtectionListView : View() {
    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            context.player.protectionUser().localPlayer.allRegions()
                .map { RegionInfo(it) }.toMutableList()
        }.elementFactory { _, builder, _, info ->
            builder.withItem(createRegionItem(info)).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    ProtectionInfoView::class.java,
                    ImmutableMap.of("region-info", info)
                )
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
                "OOOOPBNOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(ProtectionMainView::class.java)
        }

        render.layoutSlot('O', outlineItem)

        render
            .layoutSlot('P')
            .updateOnStateChange(paginationState)
            .onRender { slotRender ->
                slotRender.item = if (pagination.canBack()) previousItem else outlineItem
            }
            .onClick { context ->
                pagination.back()
                pagination.update()
                context.playNewPageSound()
            }

        render
            .layoutSlot('N')
            .updateOnStateChange(paginationState)
            .onRender { slotRender ->
                slotRender.item = if (pagination.canAdvance()) nextItem else outlineItem
            }
            .onClick { context ->
                pagination.advance()
                pagination.update()
                context.playNewPageSound()
            }
    }
}

@Suppress("UnstableApiUsage")
fun createRegionItem(info: RegionInfo) = buildItem(Material.GRASS_BLOCK) {
    displayName {
        protectColored(info.name.toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        emptyLine()
        line {
            protectColored("Grundstückinformationen".toSmallCaps(), TextDecoration.BOLD)
        }
        line {
            spacer("-")
            appendSpace()
            protectColored("Größe: ")
            variableValue("${info.volume} Blöcke²")
        }
        val ownerNames = info.region.getOwnerNames().toList()
        if (ownerNames.isNotEmpty()) {
            line {
                spacer("-")
                appendSpace()
                protectColored("Besitzer: ")
                variableValue(ownerNames.joinToString(", "))
            }
        }
        val memberNames = info.region.getMemberNames().toList()
        line {
            spacer("-")
            appendSpace()
            protectColored("Mitglieder: ")
            variableValue(if (memberNames.isEmpty()) "Keine" else memberNames.joinToString(", "))
        }
        emptyLine()
    }
}