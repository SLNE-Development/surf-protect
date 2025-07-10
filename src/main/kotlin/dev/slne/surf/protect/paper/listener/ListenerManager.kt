package dev.slne.surf.protect.paper.listener

import com.sk89q.worldguard.WorldGuard
import dev.slne.surf.protect.paper.listener.listeners.BorderCrossingHandler
import dev.slne.surf.protect.paper.listener.listeners.ProtectionHotbarListener
import dev.slne.surf.protect.paper.listener.listeners.ProtectionModeListener
import dev.slne.surf.protect.paper.listener.listeners.RegionListener
import dev.slne.surf.surfapi.bukkit.api.event.register

object ListenerManager {

    fun registerListeners() {
        ProtectionModeListener.register()
        ProtectionHotbarListener.register()
        RegionListener.register()

        val sessionManager = WorldGuard.getInstance().platform.sessionManager
        sessionManager.registerHandler(BorderCrossingHandler.Factory, null)
    }

    fun unregisterListeners() {
        val sessionManager = WorldGuard.getInstance().platform.sessionManager
        sessionManager.unregisterHandler(BorderCrossingHandler.Factory)
    }
}
