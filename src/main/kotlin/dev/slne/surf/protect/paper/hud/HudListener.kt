package dev.slne.surf.protect.paper.hud

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

object HudListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        HudManager.show(event.player)
    }
}