package dev.slne.surf.protect.paper.menu.view.list

import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.menu.view.ProtectionMainView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.allRegions
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration

@Suppress("UnstableApiUsage")
object ProtectionListView : View() {
    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            context.player.protectionUser().localPlayer.allRegions()
                .map { RegionInfo(it) }.toMutableList()
        }.elementFactory { _, builder, _, protection ->
            builder.renderWith {
                createRegionItem(protection)
            }
                .onClick { context ->
                    context.playGeneralClickSound()
                    context.openForPlayer(
                        ProtectionInfoView::class.java,
                        mapOf("protection" to protection)
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
                "OOPOBONOO"
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
            .watch(paginationState)
            .renderWith {
                if (pagination.canBack()) {
                    backItem
                } else {
                    outlineItem
                }
            }
            .onClick { context ->
                if (pagination.canBack()) {
                    pagination.back()
                    context.playNewPageSound()
                }
            }

        render
            .layoutSlot('N')
            .watch(paginationState)
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    outlineItem
                }
            }
            .onClick { context ->
                if (pagination.canAdvance()) {
                    pagination.advance()
                    context.playNewPageSound()
                }
            }
    }
}