package dev.slne.surf.protect.paper.menu.view.members

import com.destroystokyo.paper.profile.PlayerProfile
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import io.papermc.paper.registry.data.dialog.DialogBase
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.`object`.ObjectContents

@Suppress("UnstableApiUsage")
fun protectionMemberRemoveConfirmationDialog(regionInfo: RegionInfo, member: PlayerProfile, onRemoved: () -> Unit) =
    dialog {
        base {
            afterAction(DialogBase.DialogAfterAction.CLOSE)
            title {
                protectColored("Grundstück — Mitglied entfernen".toSmallCaps(), TextDecoration.BOLD)
            }
            body {
                plainMessage {
                    warning("Bist du dir sicher, dass du ")
                    variableValue(member.name ?: member.id?.toString() ?: "#Unbekannt")
                    appendSpace()
                    append(Component.`object`(ObjectContents.playerHead(member)))
                    warning(" vom Grundstück entfernen möchtest?")
                }
            }
        }

        type {
            confirmation {
                yes {
                    label {
                        success("Bestätigen")
                    }
                    action {
                        callback {
                            val uuid = member.id
                            if (uuid != null) {
                                regionInfo.region.members.removePlayer(member.id)
                                ProtectionVisualizerManager.onRegionMemberChange(regionInfo.region)
                            }

                            it.sendText {
                                appendSuccessPrefix()
                                success("Du hast das Mitglied ")
                                variableValue(member.name ?: uuid?.toString() ?: "#Unbekannt")
                                success(" entfernt.")
                            }
                            onRemoved()
                        }
                    }
                }
                no {
                    label {
                        error("Abbrechen")
                    }
                }
            }
        }
    }