package dev.slne.surf.protect.paper.menu.view.members

import dev.slne.surf.protect.paper.menu.dialog.protectionAddMemberDialog
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
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
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemType
import org.bukkit.inventory.meta.SkullMeta

@Suppress("UnstableApiUsage")
object ProtectionMemberListView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")

    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            protectionState.get(context).members.map { Bukkit.getOfflinePlayer(it.uniqueId) }
                .toMutableList()
        }.elementFactory { _, builder, _, player ->
            builder.withItem(buildItem(Material.PLAYER_HEAD) {
                displayName {
                    protectColored(player.name ?: "#Unbekannt".toSmallCaps())
                }

                editMeta(SkullMeta::class.java) {
                    it.owningPlayer = player
                }
            }).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    ProtectionMemberRemoveConfirmView::class.java, mapOf(
                        "protection" to protectionState.get(context),
                        "member" to player
                    )
                )
            }
        }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Mitglieder".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOAOOOO",
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
        render.layoutSlot('A', addMemberItem).onClick { context ->
            context.playGeneralClickSound()
            context.closeForPlayer()
            context.player.showDialog(protectionAddMemberDialog(protectionState.get(context)))
        }

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

    val addMemberItem = ItemType.PLAYER_HEAD.createItemStack().apply {
        displayName {
            protectColored("Mitglied hinzufügen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Klicke hier, um ein neues Mitglied hinzuzufügen.")
            }
        }
    }
}