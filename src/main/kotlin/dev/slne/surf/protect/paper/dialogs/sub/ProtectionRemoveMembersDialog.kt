@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.dialogs.sub

import com.google.common.collect.ImmutableMap
import com.sk89q.worldguard.LocalPlayer
import dev.slne.surf.protect.paper.menu.view.member.ProtectionMemberView
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizerManager
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import net.kyori.adventure.text.Component
import org.bukkit.OfflinePlayer

object ProtectionRemoveMembersDialog {

    fun removeMembersDialog(target: OfflinePlayer, info: RegionInfo): Dialog = dialog {
        base {
            title { primary("Protection — Mitglieder entfernen") }
            externalTitle { text("Mitglieder entfernen") }
            afterAction(DialogBase.DialogAfterAction.NONE)
            body {
                plainMessage(400) {
                    info("Wähle ein oder mehrere Mitglieder aus, die du entfernen möchtest.")
                }
            }
            input {
                for (member in info.members) {
                    boolean(member.uniqueId.toString().replace("-", "")) {
                        label { text(member.name ?: member.uniqueId.toString()) }
                        initial(false)
                    }
                }
            }
        }
        type {
            confirmation(createConfirmationButton(target, info), createBackButton(info, target))
        }
    }

    private fun createBackButton(info: RegionInfo, target: OfflinePlayer) = actionButton {
        label { text("Zurück") }
        action {
            playerCallback { player ->
                plugin.launch {
                    viewFrame.open(
                        ProtectionMemberView::class.java,
                        player,
                        ImmutableMap.of("region-info", info)
                    )
                }
            }
        }
    }

    private fun createConfirmationButton(target: OfflinePlayer, info: RegionInfo) = actionButton {
        label { success("Entfernen") }
        action {
            customClick { response, viewer ->
                val membersToRemove = info.members.filter { member ->
                    response.getBoolean(member.uniqueId.toString().replace("-", "")) ?: false
                }

                if (membersToRemove.isEmpty()) {
                    viewer.sendText {
                        appendErrorPrefix()
                        error("Du hast keine Mitglieder ausgewählt, die entfernt werden sollen.")
                    }
                    viewer.showDialog(removeMembersDialog(target, info))
                    return@customClick
                }

                val members = info.region.members
                for (member in membersToRemove) {
                    members.removePlayer(member)
                }

                ProtectionVisualizerManager.onRegionMemberChange(info.region)

                viewer.sendText {
                    appendSuccessPrefix()
                    success("Die folgenden Mitglieder wurden entfernt: ")
                    appendCollection(membersToRemove) {
                        Component.text(it.name ?: it.uniqueId.toString(), Colors.VARIABLE_VALUE)
                    }
                }
                plugin.launch {
                    viewFrame.open(
                        ProtectionMemberView::class.java,
                        viewer,
                        ImmutableMap.of("region-info", info)
                    )
                }
            }
        }
    }
}