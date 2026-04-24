package dev.slne.surf.protect.paper.menu.view

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
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
                "OOOOIOOOO",
                "O       O",
                "ORM F SEO",
                "O       O",
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
        render.layoutSlot('I', createRegionItem(protectionState.get(render), false))
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
            protectColored("Mitglieder".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                spacer("Klicke, um die Mitglieder zu verwalten".toSmallCaps())
            }
        }
    }

    private val sellItem = ItemType.EMERALD.createItemStack().apply {
        displayName {
            protectColored("Verkaufen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                spacer("Klicke, um das Grundstück zu verkaufen".toSmallCaps())
            }
        }
    }

    private val renameItem = ItemType.NAME_TAG.createItemStack().apply {
        displayName {
            protectColored("Umbenennen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                spacer("Klicke, um das Grundstück umzubenennen".toSmallCaps())
            }
        }
    }

    private val expandItem = ItemType.OAK_SIGN.createItemStack().apply {
        displayName {
            protectColored("Erweitern".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                spacer("Klicke, um das Grundstück zu erweitern".toSmallCaps())
            }
        }
    }

    private val editFlags = ItemType.REDSTONE_TORCH.createItemStack().apply {
        displayName {
            protectColored("Flags bearbeiten".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                spacer("Klicke, um die Flags zu bearbeiten".toSmallCaps())
            }
        }
    }
}

