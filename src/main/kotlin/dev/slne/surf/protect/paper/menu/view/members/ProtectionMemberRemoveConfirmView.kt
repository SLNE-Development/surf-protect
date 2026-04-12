package dev.slne.surf.protect.paper.menu.view.members

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.meta.SkullMeta

object ProtectionMemberRemoveConfirmView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")
    val memberState: State<OfflinePlayer> = initialState("member")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Mitglied entfernen".toSmallCaps(), TextDecoration.BOLD)
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
        render.layoutSlot('I', createMemberItem(memberState.get(render)))
        render.layoutSlot('C', cancelItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionMemberListView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('Y', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val protection = protectionState.get(click)
            val member = memberState.get(click)

            protection.region.members.removePlayer(member.uniqueId)
            ProtectionVisualizerManager.onRegionMemberChange(protection.region)
            click.openForPlayer(
                ProtectionMemberListView::class.java,
                mapOf("protection" to protection)
            )

            click.player.sendText {
                appendSuccessPrefix()
                success("Du hast das Mitglied ")
                variableValue(member?.name ?: "#Unbekannt")
                success(" entfernt.")
            }
        }
    }

    private fun createMemberItem(member: OfflinePlayer) = buildItem(Material.PLAYER_HEAD) {
        displayName {
            protectColored(member.name?.toSmallCaps() ?: "#Unknown", TextDecoration.BOLD)
        }

        editMeta(SkullMeta::class.java) {
            it.owningPlayer = member
        }

        buildLore {
            line {
                darkSpacer(member.uniqueId.toString())
            }
            emptyLine()
            line {
                darkSpacer("Klicke, um das Mitglied zu entfernen")
            }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("Abbrechen".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            line {
                darkSpacer("Klicke, um den Vorgang abzubrechen")
            }
        }
    }

    private val confirmItem = buildItem(Material.GREEN_STAINED_GLASS_PANE) {
        displayName {
            success("Bestätigen".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            line {
                darkSpacer("Klicke, um das Mitglied zu entfernen")
            }
        }
    }
}