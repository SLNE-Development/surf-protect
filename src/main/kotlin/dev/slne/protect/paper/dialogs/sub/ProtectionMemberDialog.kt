@file:Suppress("UnstableApiUsage")

package dev.slne.protect.paper.dialogs.sub

import dev.slne.protect.paper.region.info.RegionInfo
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.registry.data.dialog.DialogBase
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemType

object ProtectionMemberDialog {

    fun createProtectionMemberDialog(target: OfflinePlayer, info: RegionInfo) =
        dialog {
            base {
                title { primary("Protection — Mitglieder") }
                afterAction(DialogBase.DialogAfterAction.NONE)
                body {
                    for (member in info.members) {
                        item {
                            showTooltip(false)
                            item(ItemType.PLAYER_HEAD) {
                                setData(
                                    DataComponentTypes.PROFILE,
                                    ResolvableProfile.resolvableProfile()
                                        .uuid(member.uniqueId)
                                        .name(member.name)
                                )
                            }
                            simpleDescription {
                                variableValue(member.name ?: member.uniqueId.toString())
                            }
                        }
                    }
                }
            }

            type {
                dialogList {
                    dialog(ProtectionAddMemberDialog.addMemberDialog(target, info))
                    dialog(ProtectionRemoveMembersDialog.removeMembersDialog(target, info))
                    exitAction(createBackButton(info, target))
                }
            }
        }

    private fun createBackButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback {
                it.showDialog(ProtectionInfoDialog.createProtectionInfoDialog(it, info, target))
            }
        }
    }
}