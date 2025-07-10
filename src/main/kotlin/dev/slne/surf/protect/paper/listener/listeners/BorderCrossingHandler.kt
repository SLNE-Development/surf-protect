package dev.slne.surf.protect.paper.listener.listeners

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.util.Location
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.session.MoveType
import com.sk89q.worldguard.session.Session
import com.sk89q.worldguard.session.handler.Handler
import dev.slne.surf.protect.paper.message.Messages
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.settings.ProtectionUserSettings

class BorderCrossingHandler(session: Session) : Handler(session) {
    override fun onCrossBoundary(
        player: LocalPlayer,
        from: Location?,
        to: Location?,
        toSet: ApplicableRegionSet?,
        entered: MutableSet<ProtectedRegion>,
        exited: MutableSet<ProtectedRegion>,
        moveType: MoveType?
    ): Boolean {
        for (region in entered) {
            handleMessage(player, region, entered = true)
        }

        for (region in exited) {
            handleMessage(player, region, entered = false)
        }

        return true
    }

    private fun handleMessage(
        localPlayer: LocalPlayer,
        region: ProtectedRegion,
        entered: Boolean
    ) {
        val infoFlag = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG) ?: return
        val protectionFlag = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) ?: return
        if (protectionFlag == StateFlag.State.DENY) return

        val player = BukkitAdapter.adapt(localPlayer)
        if (!ProtectionUserSettings.PLOT_MESSAGES.getValue(player)) {
            return
        }

        player.sendMessage(Messages.BorderCrossing.borderCrossing(infoFlag.name, entered))
    }

    object Factory : Handler.Factory<BorderCrossingHandler>() {
        override fun create(session: Session): BorderCrossingHandler {
            return BorderCrossingHandler(session)
        }
    }
}
