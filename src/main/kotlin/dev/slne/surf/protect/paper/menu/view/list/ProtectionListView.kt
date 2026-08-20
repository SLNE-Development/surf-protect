package dev.slne.surf.protect.paper.menu.view.list

import dev.slne.surf.api.paper.inventory.framework.dsl.onItemClick
import dev.slne.surf.api.paper.inventory.framework.dsl.openForPlayer
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.protect.paper.menu.util.createRegionItem
import dev.slne.surf.protect.paper.menu.util.playGeneralClickSound
import dev.slne.surf.protect.paper.menu.view.ProtectionViewConstants
import dev.slne.surf.protect.paper.menu.view.protectionInfoView
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.protect.paper.util.allRegions

val protectionListView = paginatedSurfView("Deine Grundstücke") {
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
}