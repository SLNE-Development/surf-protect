package dev.slne.surf.protect.paper.menu.view.list

import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.protect.paper.menu.util.createRegionItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.view.ProtectionViewConstants
import dev.slne.surf.protect.paper.menu.view.protectionInfoView
import dev.slne.surf.protect.paper.menu.view.protectionMainView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.allRegions

val protectionListView: AbstractPaginatedSurfView = paginatedSurfView("Meine Grundstücke") {
    settings {
        paginationViewRows(PaginationViewRows.THREE)
    }

    layoutTarget('R')
    pagination {
        lazySource { it.player.protectionUser().localPlayer.allRegions().map(::RegionInfo) }
        itemFactory { regionInfo ->
            withItem(createRegionItem(regionInfo))
            onItemClick {
                playGeneralClickSound()
                openForPlayer(
                    protectionInfoView,
                    mapOf(ProtectionViewConstants.REGION_INFO_STATE to regionInfo)
                )
            }
        }
    }

    onFirstRender {
        slot(4, 1, ViewIcon(ViewIconType.CROSS, ViewIconColor.RED).build {
            displayName {
                error("Zurück")
            }
        }).onClick { click ->
            click.openForPlayer(
                protectionMainView::class.java
            )
        }
    }
}