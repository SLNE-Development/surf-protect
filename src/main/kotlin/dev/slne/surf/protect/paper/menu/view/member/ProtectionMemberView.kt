package dev.slne.surf.protect.paper.menu.view.member

import com.google.common.collect.ImmutableMap
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionAddMemberDialog
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionRemoveMembersDialog
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType

@Suppress("UnstableApiUsage")
object ProtectionMemberView : View() {
    private val regionInfoState = initialState<RegionInfo>("region-info")

    private val paginationState: State<Pagination> =
        buildLazyPaginationState { context ->
            regionInfoState.get(context).members.toMutableList()
        }.elementFactory { context, builder, _, member ->
            val item = ItemType.PLAYER_HEAD.createItemStack().apply {
                setData(
                    DataComponentTypes.PROFILE,
                    ResolvableProfile.resolvableProfile()
                        .uuid(member.uniqueId)
                        .name(member.name)
                        .build()
                )
                displayName {
                    protectColored(member.name ?: member.uniqueId.toString(), TextDecoration.BOLD)
                }
            }
            builder.withItem(item).onClick { click ->
                click.playGeneralClickSound()
            }
        }.layoutTarget('M').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Mitglieder".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "OMMMMMMMO",
                "OMMMMMMMO",
                "OMMMMMMMO",
                "OAOPBNROO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val info = regionInfoState.get(render)
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionInfoView::class.java,
                ImmutableMap.of("region-info", info)
            )
        }

        render.layoutSlot('A', addMemberItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            click.player.showDialog(ProtectionAddMemberDialog.addMemberDialog(click.player, info))
        }

        render.layoutSlot('R', removeMembersItem).onClick { click ->
            click.playGeneralClickSound()
            click.closeForPlayer()
            click.player.showDialog(ProtectionRemoveMembersDialog.removeMembersDialog(click.player, info))
        }

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

    private val addMemberItem = MenuHeads.PLUS.clone().apply {
        displayName {
            success("Mitglied hinzufügen".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Füge ein neues Mitglied hinzu") }
            emptyLine()
        }
    }

    private val removeMembersItem = MenuHeads.MINUS.clone().apply {
        displayName {
            error("Mitglieder entfernen".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Entferne Mitglieder aus dem Grundstück") }
            emptyLine()
        }
    }
}
