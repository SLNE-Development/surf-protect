package dev.slne.surf.protect.paper.hud

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.hud.api.hud
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.getProtectedRegions
import org.bukkit.entity.Player

object HudManager {
    private const val HUD_ID = "protect-current-region"

    fun show(player: Player) {
        val region = player.location.getProtectedRegions(false).firstOrNull()
        val text = if (region == null) {
            "Wilderness"
        } else {
            RegionInfo(region).name
        }

        player.hud.add {
            line(0) {
                element(HUD_ID, 0, buildText {
                    info(text)
                })
            }
        }
    }
}