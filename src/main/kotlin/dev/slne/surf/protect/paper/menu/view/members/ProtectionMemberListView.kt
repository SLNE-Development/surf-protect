package dev.slne.surf.protect.paper.menu.view.members

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.slot
import dev.slne.surf.api.paper.inventory.framework.dsl.withItem
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.icon.viewIcon
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.protect.paper.menu.dialog.protectionAddMemberDialog
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.util.protectColored
import dev.slne.surf.protect.paper.menu.view.ProtectionViewConstants
import dev.slne.surf.protect.paper.region.info.RegionInfo
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.inventory.ItemType


@Suppress("UnstableApiUsage")
val protectionMemberListView = paginatedSurfView("Mitglieder") {
    settings {
        paginationViewRows(PaginationViewRows.THREE)
    }

    layoutTarget('M')

    val regionInfoState = initialState<RegionInfo>(ProtectionViewConstants.REGION_INFO_STATE)
    pagination {
        computedSource { context ->
            regionInfoState[context]
                .members
                .map { member ->
                    server.createProfile(member.uniqueId, member.name)
                        .also { it.completeFromCache() }
                }
        }

        itemFactory { profile ->
            val uuid = profile.id ?: return@itemFactory run {
                withItem(ItemType.BARRIER) {
                    displayName {
                        protectColored("Unbekannt".toSmallCaps())
                    }
                }
            }

            withItem(ItemType.PLAYER_HEAD) {
                displayName {
                    protectColored(profile.name ?: uuid.toString())
                }
                buildLore {
                    line {
                        darkSpacer(uuid.toString())
                    }
                    emptyLine()
                    line {
                        info("Klicke, um das Mitglied zu entfernen")
                    }
                }
                setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile))
            }

            onItemClick {
                playGeneralClickSound()
                player.showDialog(
                    protectionMemberRemoveConfirmationDialog(
                        regionInfoState[this],
                        profile
                    ) { update() }
                )
            }
        }
    }

    onFirstRender {
        slot(4, 9) {
            withItem(
                viewIcon(ViewIconType.PLUS, ViewIconColor.GREEN) {
                    displayName {
                        text(
                            "Mitglied hinzufügen".toSmallCaps(),
                            TextColor.fromHexString("#91CE22"),
                            TextDecoration.BOLD
                        )
                    }

                    buildLore {
                        emptyLine()
                        line {
                            spacer("Klicke hier, um ein neues Mitglied hinzuzufügen.")
                        }
                    }
                }
            )

            onItemClick {
                playGeneralClickSound()
                player.showDialog(protectionAddMemberDialog(regionInfoState[this]) { update() })
            }
        }
    }
}