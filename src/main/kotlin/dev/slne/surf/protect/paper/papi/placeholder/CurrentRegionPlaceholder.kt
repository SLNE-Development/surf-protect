package dev.slne.surf.protect.paper.papi.placeholder

import dev.slne.surf.api.paper.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.getProtectedRegions
import org.bukkit.OfflinePlayer

object CurrentRegionPlaceholder : PapiPlaceholder("current-region") {
    override fun parse(
        player: OfflinePlayer,
        args: List<String>
    ): String? {
        val player = player.player ?: return null
        val region = player.location.getProtectedRegions(false).firstOrNull()

        return if (region == null) {
            "Wilderness"
        } else {
            RegionInfo(region).name
        }
    }
}