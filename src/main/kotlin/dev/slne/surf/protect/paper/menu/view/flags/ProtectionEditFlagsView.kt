package dev.slne.surf.protect.paper.menu.view.flags

import com.sk89q.worldguard.protection.flags.RegionGroup
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.renderWith
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.protect.paper.menu.util.appendBlob
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.ProtectionViewConstants
import dev.slne.surf.protect.paper.region.flags.EditableProtectionFlags
import dev.slne.surf.protect.paper.region.info.RegionInfo
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemStack

val protectionEditFlagsView = paginatedSurfView("Grundstück - Flags") {
    settings {
        paginationViewRows(PaginationViewRows.THREE)
        navigateBackOnOutsideClick()
    }
    layoutTarget('F')

    val regionInfoState = initialState<RegionInfo>(ProtectionViewConstants.REGION_INFO_STATE)

    pagination {
        source(EditableProtectionFlags.entries)
        elementFactory { context, builder, _, flag ->
            builder.onItemClick {
                val regionInfo = regionInfoState[this]
                handleOnItemClick(regionInfo, flag)
            }

            builder.renderWith(flag.icon) {
                val regionInfo = regionInfoState[context]
                renderFlagItem(regionInfo, flag)
            }
        }
    }
}

private fun ItemStack.renderFlagItem(
    regionInfo: RegionInfo,
    flag: EditableProtectionFlags
) {
    val currentState = getCurrentState(regionInfo.region, flag)

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
                if (currentState == StateFlag.State.ALLOW) {
                    success("Alle".toSmallCaps())
                } else {
                    variableValue("Mitglieder".toSmallCaps())
                }
            } else {
                if (currentState == StateFlag.State.ALLOW) {
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

private fun SlotClickContext.handleOnItemClick(
    regionInfo: RegionInfo,
    flag: EditableProtectionFlags
) {
    val region = regionInfo.region

    val newState = toggleState(region, flag)
    applyState(region, flag, newState)
    update()

    playGeneralClickSound()
    player.sendText {
        appendSuccessPrefix()
        success("Du hast die Flag ")
        protectColored(flag.displayName.toSmallCaps(), TextDecoration.BOLD)
        success(" auf ")
        variableValue(formatState(flag, newState))
        success(" gesetzt.")
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