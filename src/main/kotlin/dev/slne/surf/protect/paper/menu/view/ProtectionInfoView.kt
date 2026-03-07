package dev.slne.surf.protect.paper.menu.view

import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionEditFlagsDialog
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionExpandDialog
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionRenameDialog
import dev.slne.surf.protect.paper.dialogs.sub.ProtectionSellDialog
import dev.slne.surf.protect.paper.menu.util.*
import dev.slne.surf.protect.paper.menu.view.member.ProtectionMemberView
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.getMemberNames
import dev.slne.surf.protect.paper.util.getOwnerNames
import dev.slne.surf.protect.paper.util.toBukkitLocation
import dev.slne.surf.protect.paper.util.world
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialogWithBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import kotlinx.coroutines.future.await
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

@Suppress("UnstableApiUsage")
object ProtectionInfoView : View() {
    private val regionInfoState = initialState<RegionInfo>("region-info")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                protectColored("Grundstück".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O   I   O",
                "O       O",
                "O T R E O",
                "OFMOSBOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val info = regionInfoState.get(render)
        val center = info.centerLocation
        val bukkitCenter = center?.toBukkitLocation()

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('I', regionInfoItem(info, render)).onClick { click ->
            click.playGeneralClickSound()
        }

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(ProtectionListView::class.java)
        }

        if (bukkitCenter != null && render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_TELEPORT)) {
            render.layoutSlot('T', teleportItem).onClick { click ->
                click.playGeneralClickSound()
                plugin.launch {
                    val world = bukkitCenter.world
                    val chunk = world.getChunkAtAsync(bukkitCenter).await()
                    val highestY = chunk.getChunkSnapshot(true, false, false)
                        .getHighestBlockYAt(bukkitCenter.blockX and 15, bukkitCenter.blockZ and 15)
                    val tpLocation = bukkitCenter.clone().apply { y = highestY + 1.0 }
                    click.player.teleportAsync(tpLocation).await()
                    click.player.showDialog(teleportSuccessNotice())
                }
            }
        } else {
            render.layoutSlot('T', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_RENAME)) {
            render.layoutSlot('R', renameItem).onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                click.player.showDialog(ProtectionRenameDialog.createProtectionRenameDialog(info, click.player))
            }
        } else {
            render.layoutSlot('R', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_EXPAND)) {
            render.layoutSlot('E', expandItem).onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                click.player.showDialog(ProtectionExpandDialog.createProtectionExpandConfirmationDialog(info, click.player))
            }
        } else {
            render.layoutSlot('E', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_EDIT_FLAGS)) {
            render.layoutSlot('F', flagsItem).onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                click.player.showDialog(ProtectionEditFlagsDialog.createEditFlagsDialog(info, click.player))
            }
        } else {
            render.layoutSlot('F', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_MEMBER)) {
            render.layoutSlot('M', membersItem).onClick { click ->
                click.playGeneralClickSound()
                click.openForPlayer(
                    ProtectionMemberView::class.java,
                    ImmutableMap.of("region-info", info)
                )
            }
        } else {
            render.layoutSlot('M', outlineItem)
        }

        if (render.player.hasPermission(ProtectPermissionRegistry.PROTECTION_SELL)) {
            render.layoutSlot('S', sellItem).onClick { click ->
                click.playGeneralClickSound()
                click.closeForPlayer()
                click.player.showDialog(ProtectionSellDialog.createSellDialog(info, click.player))
            }
        } else {
            render.layoutSlot('S', outlineItem)
        }
    }

    private fun teleportSuccessNotice() =
        noticeDialogWithBuilder(
            text("Protection Info — Teleportation", Colors.PRIMARY)
        ) {
            success("Du wurdest erfolgreich zu der Protection teleportiert.")
        }

    private fun regionInfoItem(info: RegionInfo, render: RenderContext) = buildItem(Material.GRASS_BLOCK) {
        displayName {
            protectColored(info.name.toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                protectColored("Grundstückinformationen".toSmallCaps(), TextDecoration.BOLD)
            }
            line {
                spacer("-")
                appendSpace()
                protectColored("Größe: ")
                variableValue("${info.volume} Blöcke²")
            }
            val center = info.centerLocation
            val bukkitCenter = center?.toBukkitLocation()
            if (center != null) {
                line {
                    spacer("-")
                    appendSpace()
                    protectColored("Ort: ")
                    variableValue("${center.blockX}, ${center.blockY}, ${center.blockZ} — ${center.world.name ?: "Unbekannt"}")
                }
                if (bukkitCenter != null && bukkitCenter.world == render.player.world) {
                    line {
                        spacer("-")
                        appendSpace()
                        protectColored("Entfernung: ")
                        variableValue("${render.player.location.distance(bukkitCenter).toLong()} Blöcke")
                    }
                }
            }
            val ownerNames = info.region.getOwnerNames().toList()
            if (ownerNames.isNotEmpty()) {
                line {
                    spacer("-")
                    appendSpace()
                    protectColored("Besitzer: ")
                    variableValue(ownerNames.joinToString(", "))
                }
            }
            val memberNames = info.region.getMemberNames().toList()
            line {
                spacer("-")
                appendSpace()
                protectColored("Mitglieder: ")
                variableValue(if (memberNames.isEmpty()) "Keine" else memberNames.joinToString(", "))
            }
            emptyLine()
        }
    }

    private val teleportItem = buildItem(Material.ENDER_PEARL) {
        displayName {
            primary("Teleportieren".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Teleportiere dich zum Grundstück") }
            emptyLine()
        }
    }

    private val renameItem = buildItem(Material.NAME_TAG) {
        displayName {
            primary("Umbenennen".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Benenne das Grundstück um") }
            emptyLine()
        }
    }

    private val expandItem = buildItem(Material.PISTON) {
        displayName {
            primary("Erweitern".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Erweitere das Grundstück") }
            emptyLine()
        }
    }

    private val flagsItem = buildItem(Material.COMPARATOR) {
        displayName {
            primary("Flags bearbeiten".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Bearbeite die Flags des Grundstücks") }
            emptyLine()
        }
    }

    private val membersItem = buildItem(Material.PLAYER_HEAD) {
        displayName {
            primary("Mitglieder".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Verwalte die Mitglieder des Grundstücks") }
            emptyLine()
        }
    }

    private val sellItem = buildItem(Material.GOLD_INGOT) {
        displayName {
            error("Verkaufen".toSmallCaps(), TextDecoration.BOLD)
        }
        buildLore {
            emptyLine()
            line { spacer("Verkaufe das Grundstück") }
            emptyLine()
        }
    }
}
