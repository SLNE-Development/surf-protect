package dev.slne.surf.protect.paper.listener

import com.sk89q.worldguard.WorldGuard
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.protect.paper.listener.listeners.*

object ListenerManager {

    fun registerListeners() {
        ProtectionModeListener.register()
        ProtectionHotbarListener.register()
        RegionListener.register()
        ProtectionExplosionListener.register()

        val sessionManager = WorldGuard.getInstance().platform.sessionManager
        sessionManager.registerHandler(BorderCrossingHandler.Factory, null)
    }

    fun unregisterListeners() {
        val sessionManager = WorldGuard.getInstance().platform.sessionManager
        sessionManager.unregisterHandler(BorderCrossingHandler.Factory)
    }
}
