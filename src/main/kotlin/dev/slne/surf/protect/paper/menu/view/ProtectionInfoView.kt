package dev.slne.surf.protect.paper.menu.view

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.dsl.slot
import dev.slne.surf.api.paper.inventory.framework.modifyConfig
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.icon.viewIcon
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.protect.paper.menu.dialog.protectionRenameDialog
import dev.slne.surf.protect.paper.menu.util.createRegionItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.playNoSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.flags.protectionEditFlagsView
import dev.slne.surf.protect.paper.menu.view.list.protectionListView
import dev.slne.surf.protect.paper.menu.view.members.protectionMemberListView
import dev.slne.surf.protect.paper.menu.view.sell.protectionSellConfirmationDialog
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.standsInProtectedRegion
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType

val protectionInfoView: AbstractSurfView = surfView("Grundstück") {
    settings {
        rows(4)
    }

    containerDefaults {
        blockRow(1)
        blockRow(2)
        blockRow(3, exemptColumns = intArrayOf(1, 2, 4, 6, 7))
        blockRow(4)
    }

    val regionInfoState = initialState<RegionInfo>(ProtectionViewConstants.REGION_INFO_STATE)

    onOpen {
        modifyConfig {
            title(regionInfoState[this@onOpen].name)
        }
    }

    onFirstRender {
        val regionInfo = regionInfoState[this]

        slot(1, 5) {
            renderWith {
                createRegionItem(
                    regionInfo,
                    baseItemStack = viewIcon(
                        ViewIconType.QUESTION_MARK,
                        ViewIconColor.BLUE
                    ),
                    showMoreInfo = false
                )
            }
        }

        slot(4, 5, ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
            displayName {
                error("Zurück")
            }
        }).onClick { click ->
            click.openForPlayer(
                protectionListView::class.java
            )
        }

        slot(3, 2) {
            withItem(renameItem)
            onItemClick {
                playGeneralClickSound()
                player.showDialog(protectionRenameDialog(regionInfoState[this]) { update() })
            }
        }
        slot(3, 3) {
            withItem(membersItem)
            onItemClick {
                playGeneralClickSound()
                openForPlayer(
                    protectionMemberListView,
                    mapOf(ProtectionViewConstants.REGION_INFO_STATE to regionInfoState[this])
                )
            }
        }

        slot(3, 5) {
            withItem(editFlags)
            onItemClick {
                playGeneralClickSound()
                openForPlayer(
                    protectionEditFlagsView,
                    mapOf(ProtectionViewConstants.REGION_INFO_STATE to regionInfoState[this])
                )
            }
        }

        slot(3, 7) {
            withItem(sellItem)
            onItemClick {
                playGeneralClickSound()
                player.showDialog(protectionSellConfirmationDialog(regionInfoState[this]) { closeForPlayer() })
            }
        }
        slot(3, 8) {
            withItem(expandItem)
            onItemClick {
                playGeneralClickSound()

                val protectionUser = player.protectionUser()
                val protection = regionInfoState[this]

                val protectionRegion = ProtectionRegion(
                    protectionUser,
                    player,
                    player.inventory.contents,
                    protection.region
                )

                plugin.launch {
                    if (player.standsInProtectedRegion(protection.region)) {
                        val started = player.protectionUser().startRegionCreation(protectionRegion)

                        if (started) {
                            withContext(plugin.entityDispatcher(player)) {
                                closeForPlayer()
                            }

                            protectionRegion.setCornerMarkers()
                            protectionUser.updateMarkerItems()
                        } else {
                            playNoSound()
                        }
                    } else {
                        playNoSound()
                        player.sendText {
                            appendErrorPrefix()
                            error("Du musst dich auf deinem Grundstück befinden, um es erweitern zu können.")
                        }
                        withContext(plugin.entityDispatcher(player)) {
                            closeForPlayer()
                        }
                    }
                }
            }
        }
    }
}


private val membersItem = viewIcon(ViewIconType.USERS, ViewIconColor.WHITE) {
    displayName {
        protectColored("Mitglieder".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Klicke, um die Mitglieder zu verwalten".toSmallCaps())
        }
    }
}

private val sellItem = ItemType.EMERALD.createItemStack().apply {
    displayName {
        protectColored("Verkaufen".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Klicke, um das Grundstück zu verkaufen".toSmallCaps())
        }
    }
}

private val renameItem = ItemType.NAME_TAG.createItemStack().apply {
    displayName {
        protectColored("Umbenennen".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Klicke, um das Grundstück umzubenennen".toSmallCaps())
        }
    }
}

private val expandItem = ItemType.OAK_SIGN.createItemStack().apply {
    displayName {
        protectColored("Erweitern".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Klicke, um das Grundstück zu erweitern".toSmallCaps())
        }
    }
}

private val editFlags = viewIcon(ViewIconType.COG, ViewIconColor.WHITE) {
    displayName {
        protectColored("Flags bearbeiten".toSmallCaps(), TextDecoration.BOLD)
    }

    buildLore {
        line {
            spacer("Klicke, um die Flags zu bearbeiten".toSmallCaps())
        }
    }
}