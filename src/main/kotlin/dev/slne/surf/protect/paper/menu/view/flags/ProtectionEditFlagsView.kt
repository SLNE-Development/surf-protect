package dev.slne.surf.protect.paper.menu.view.flags

import com.sk89q.worldguard.protection.flags.RegionGroup
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.region.flags.EditableProtectionFlags
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.component.Pagination
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.MutableState
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration

object ProtectionEditFlagsView : View() {
    val protectionState: State<RegionInfo> = initialState("protection")
    val nullRegionInfo: RegionInfo? = null
    val localProtectionState: MutableState<RegionInfo?> = mutableState(nullRegionInfo)

    private val paginationState: State<Pagination> =
        buildLazyPaginationState { _ ->
            EditableProtectionFlags.entries.toMutableList()
        }.elementFactory { context, builder, _, flag ->
            builder.renderWith {
                val protection = protectionState.get(context)
                val region = protection.region

                val currentState =
                    if (flag.isPlayerRelated) {
                        val group = region.getFlag(flag.flag.regionGroupFlag)
                        if (group == RegionGroup.MEMBERS || (region.getFlag(flag.flag) == StateFlag.State.DENY)) {
                            StateFlag.State.DENY
                        } else {
                            StateFlag.State.ALLOW
                        }
                    } else {
                        region.getFlag(flag.flag) ?: flag.initialState ?: StateFlag.State.ALLOW
                    }

                createFlagItem(flag, currentState)
            }.onClick { context ->
                val protection = protectionState.get(context)
                val region = protection.region

                val oldState =
                    if (flag.isPlayerRelated) {
                        val group = region.getFlag(flag.flag.regionGroupFlag)
                        if (group == RegionGroup.MEMBERS) StateFlag.State.DENY else StateFlag.State.ALLOW
                    } else {
                        region.getFlag(flag.flag) ?: flag.initialState ?: StateFlag.State.ALLOW
                    }

                val newState =
                    if (flag.isPlayerRelated) {
                        val group = region.getFlag(flag.flag.regionGroupFlag)
                        if (group == RegionGroup.MEMBERS) StateFlag.State.ALLOW else StateFlag.State.DENY
                    } else {
                        if (oldState == StateFlag.State.ALLOW) StateFlag.State.DENY else StateFlag.State.ALLOW
                    }

                if (flag.isPlayerRelated) {
                    region.setFlag(flag.flag, StateFlag.State.ALLOW)
                    if (newState == StateFlag.State.ALLOW) {
                        region.setFlag(flag.flag.regionGroupFlag, null)
                    } else {
                        region.setFlag(flag.flag.regionGroupFlag, RegionGroup.MEMBERS)
                    }
                } else {
                    region.setFlag(flag.flag, newState)
                }

                localProtectionState.set(protection, context)
                context.playGeneralClickSound()

                context.openForPlayer(
                    ProtectionEditFlagsView::class.java,
                    mapOf("protection" to localProtectionState.get(context))
                )

                context.player.sendText {
                    appendSuccessPrefix()
                    success("Du hast die Flag ")
                    protectColored(flag.displayName.toSmallCaps(), TextDecoration.BOLD)
                    success(" auf ")
                    variableValue(
                        if (flag.isPlayerRelated) {
                            when (newState) {
                                StateFlag.State.ALLOW -> "Für alle erlaubt"
                                StateFlag.State.DENY -> "Nur für Mitglieder"
                            }
                        } else {
                            when (newState) {
                                StateFlag.State.ALLOW -> "Erlaubt"
                                StateFlag.State.DENY -> "Verboten"
                            }
                        }
                    )
                    success(" gesetzt.")
                }
            }
        }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück - Flags".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "OOPOBONOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        localProtectionState.set(protectionState.get(render), render)

        val pagination = paginationState.get(render)

        render.layoutSlot('B', backItem).onClick { context ->
            context.openForPlayer(
                ProtectionInfoView::class.java,
                mapOf("protection" to protectionState.get(render))
            )
        }

        render.layoutSlot('O', outlineItem)

        render
            .layoutSlot('P')
            .updateOnStateChange(paginationState)
            .onRender { slotRender ->
                if (pagination.canBack()) previousItem else outlineItem
            }
            .onClick { context ->
                pagination.back()
                context.playNewPageSound()
            }

        render
            .layoutSlot('N')
            .updateOnStateChange(paginationState)
            .onRender { slotRender ->
                if (pagination.canAdvance()) nextItem else outlineItem
            }
            .onClick { context ->
                pagination.advance()
                context.playNewPageSound()
            }
    }

    private fun createFlagItem(flag: EditableProtectionFlags, state: StateFlag.State) =
        buildItem(flag.icon) {
            displayName {
                protectColored(flag.displayName.toSmallCaps(), TextDecoration.BOLD)
            }

            buildLore {
                line {
                    darkSpacer("Flag: ${flag.flag.name}".toSmallCaps())
                }
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
                    variableValue("Klicke, um die Flag zu ändern.".toSmallCaps())
                }
            }
        }
}