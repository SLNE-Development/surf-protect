package dev.slne.surf.protect.paper.menu.view.members

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.LocalPlayer
import dev.slne.surf.protect.paper.menu.util.outlineItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.meta.SkullMeta

object ProtectionMemberRemoveConfirmView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")
    val memberState: State<LocalPlayer> = initialState("member")

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
                ProtectionInfoView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
        render.layoutSlot('Y', confirmItem).onClick { click ->
            click.playGeneralClickSound()
            val protection = protectionState.get(click)
            val member = memberState.get(click)

            protection.region.members.removePlayer(member.uniqueId)
            click.openForPlayer(ProtectionInfoView::class.java, mapOf("protection" to protection))
        }
    }

    private fun createMemberItem(member: LocalPlayer) = buildItem(Material.PLAYER_HEAD) {
        displayName {
            protectColored(member.displayName)
        }

        editMeta(SkullMeta::class.java) {
            it.owningPlayer = BukkitAdapter.adapt(member)
        }

        buildLore {
            line {
                darkSpacer(member.uniqueId.toString())
            }
        }
    }

    private val cancelItem = buildItem(Material.RED_STAINED_GLASS_PANE) {
        displayName {
            error("Abbrechen")
        }
    }

    private val confirmItem = buildItem(Material.GREEN_STAINED_GLASS_PANE) {
        displayName {
            success("Bestätigen")
        }
    }
}