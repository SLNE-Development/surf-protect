package dev.slne.surf.protect.paper.menu.view.flags

import com.sk89q.worldguard.protection.flags.RegionGroup
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.flags.EditableProtectionFlags
import dev.slne.surf.protect.paper.region.info.RegionInfo
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration

object ProtectionEditFlagsViewB : View() {
    private val protectionState: State<RegionInfo> = initialState("protection")

    private val paginationState: State<Pagination> =
        buildLazyPaginationState { _ -> EditableProtectionFlags.entries.chunked(36)[1].toMutableList() }
            .elementFactory { context, builder, _, flag ->

                builder.renderWith {
                    val region = protectionState.get(context).region
                    val state = getCurrentState(region, flag)
                    createFlagItem(flag, state)
                }

                builder.onClick { context ->
                    val protection = protectionState.get(context)
                    val region = protection.region

                    val newState = toggleState(region, flag)
                    applyState(region, flag, newState)

                    context.playGeneralClickSound()

                    context.openForPlayer(
                        ProtectionEditFlagsViewB::class.java,
                        mapOf("protection" to protection)
                    )

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Du hast die Flag ")
                        protectColored(flag.displayName.toSmallCaps(), TextDecoration.BOLD)
                        success(" auf ")
                        variableValue(formatState(flag, newState))
                        success(" gesetzt.")
                    }
                }
            }
            .layoutTarget('R')
            .build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Flags".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "RRRRRRRRR",
                "RRRRRRRRR",
                "RRRRRRRRR",
                "RRRRRRRRR",
                "OOOOOOOOO",
                "    B N  "
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('B', backItem).onClick { click ->
            click.openForPlayer(
                ProtectionInfoView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('L', previousItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(
                ProtectionEditFlagsViewA::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }
    }

    private fun getCurrentState(
        region: ProtectedRegion,
        flag: EditableProtectionFlags
    ): StateFlag.State {
        return if (flag.isPlayerRelated) {
            val group = region.getFlag(flag.flag.regionGroupFlag)
            if (group == RegionGroup.MEMBERS) StateFlag.State.DENY else StateFlag.State.ALLOW
        } else {
            region.getFlag(flag.flag) ?: flag.initialState ?: StateFlag.State.ALLOW
        }
    }

    private fun toggleState(
        region: ProtectedRegion,
        flag: EditableProtectionFlags
    ): StateFlag.State {
        val current = getCurrentState(region, flag)
        return if (current == StateFlag.State.ALLOW) StateFlag.State.DENY else StateFlag.State.ALLOW
    }

    private fun applyState(
        region: ProtectedRegion,
        flag: EditableProtectionFlags,
        state: StateFlag.State
    ) {
        if (flag.isPlayerRelated) {
            region.setFlag(flag.flag, StateFlag.State.ALLOW)
            if (state == StateFlag.State.ALLOW) {
                region.setFlag(flag.flag.regionGroupFlag, null)
            } else {
                region.setFlag(flag.flag.regionGroupFlag, RegionGroup.MEMBERS)
            }
        } else {
            region.setFlag(flag.flag, state)
        }
    }

    private fun formatState(flag: EditableProtectionFlags, state: StateFlag.State): String {
        return if (flag.isPlayerRelated) {
            if (state == StateFlag.State.ALLOW) "Für alle erlaubt" else "Nur für Mitglieder"
        } else {
            if (state == StateFlag.State.ALLOW) "Erlaubt" else "Verboten"
        }
    }

    private fun createFlagItem(flag: EditableProtectionFlags, state: StateFlag.State) =
        buildItem(flag.icon) {
            displayName {
                protectColored(flag.displayName.toSmallCaps(), TextDecoration.BOLD)
            }

            buildLore {
                line { darkSpacer("Flag: ${flag.flag.name}".toSmallCaps()) }
                emptyLine()
                line {
                    appendBlob()
                    appendSpace()
                    spacer(flag.description)
                }
                emptyLine()
                line {
                    appendBlob()
                    appendSpace()
                    white("Status: ".toSmallCaps())

                    if (flag.isPlayerRelated) {
                        if (state == StateFlag.State.ALLOW) {
                            success("Alle".toSmallCaps())
                        } else {
                            variableValue("Mitglieder".toSmallCaps())
                        }
                    } else {
                        if (state == StateFlag.State.ALLOW) {
                            success("Erlaubt".toSmallCaps())
                        } else {
                            error("Verboten".toSmallCaps())
                        }
                    }
                }
                emptyLine()
                line {
                    appendSpace()
                    variableValue("Klicke zum Umschalten".toSmallCaps())
                }
            }
        }
}