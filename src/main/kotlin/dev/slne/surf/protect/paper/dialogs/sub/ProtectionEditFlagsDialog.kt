@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.protect.paper.gui.protection.flags.ProtectionFlagsMap
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.clickOpensUrl
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.OfflinePlayer

object ProtectionEditFlagsDialog {

    fun createEditFlagsDialog(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Flags") }
            preventClosingWithEscape()
            afterAction(DialogBase.DialogAfterAction.NONE)
            body {
                plainMessage(400) {
                    info("Eine Liste aller Flags und ihrer Funktionen findest du in unserer Dokumentation: ")
                    appendNewline {
                        variableValue("https://server.castcrafter.de/plot-edit-flags.html#plot-flags")
                        clickOpensUrl("https://server.castcrafter.de/plot-edit-flags.html#plot-flags")
                    }
                }
            }
            input {
                val region = info.region
                for (flag in ProtectionFlagsMap.entries) {
                    val flagValue = region.getFlag(flag.flag) ?: flag.initialState
                    val state = State.fromStateFlag(flagValue)
                    singleOption(flag.ordinal.toString()) {
                        label {
                            append(flag.displayName.color(null))
                            hoverEvent(flag.description)
                        }
                        for (possible in State.entries) {
                            option(
                                possible.ordinal.toString(),
                                text(possible.displayName, Colors.VARIABLE_VALUE),
                                possible == state
                            )
                        }
                    }
                }
            }
        }
        type {
            confirmation(createSaveButton(info, target), createBackButton(info, target))
        }
    }

    private fun createSaveButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { success("Speichern") }
        action {
            customClick { response, viewer ->
                val region = info.region
                for (flag in ProtectionFlagsMap.entries) {
                    val newValueOrdinal =
                        response.getText(flag.ordinal.toString())?.toIntOrNull() ?: continue
                    val newValue = State.entries.getOrNull(newValueOrdinal) ?: continue
                    val state = newValue.toStateFlag()

                    // Quick fix that enables TNT when toggling other explosions
                    if (flag.flag == Flags.OTHER_EXPLOSION) {
                        region.setFlag(Flags.TNT, state)
                    }
                    region.setFlag(flag.flag, state)
                }
                viewer.showDialog(createFlagsSavedNotice(info, target))
            }
        }
    }

    private fun createFlagsSavedNotice(info: RegionInfo, target: OfflinePlayer) = dialog {
        base {
            title { primary("Protection — Flags gespeichert") }
            afterAction(DialogBase.DialogAfterAction.NONE)
            body {
                plainMessage {
                    success("Die Flags wurden erfolgreich gespeichert.")
                }
            }
        }
        type {
            notice(createBackButton(info, target))
        }
    }

    private fun createBackButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback {
                it.showDialog(
                    ProtectionInfoDialog.createProtectionInfoDialog(
                        it,
                        info,
                        target
                    )
                )
            }
        }
    }

    enum class State(val displayName: String) {
        ALLOW("Erlauben"),
        DENY("Verbieten"),
        UNSET("Nicht gesetzt");

        fun toStateFlag(): StateFlag.State? = when (this) {
            ALLOW -> StateFlag.State.ALLOW
            DENY -> StateFlag.State.DENY
            UNSET -> null
        }

        companion object {
            fun fromStateFlag(stateFlag: StateFlag.State?): State = when (stateFlag) {
                StateFlag.State.ALLOW -> ALLOW
                StateFlag.State.DENY -> DENY
                null -> UNSET
            }
        }
    }
}