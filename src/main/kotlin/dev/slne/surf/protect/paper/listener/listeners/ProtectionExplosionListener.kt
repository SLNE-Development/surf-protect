package dev.slne.surf.protect.paper.listener.listeners

import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.util.getProtectedRegions
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent

object ProtectionExplosionListener : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBlockExplode(event: BlockExplodeEvent) {
        if (event.explodedBlockState.type != Material.TNT) {
            return
        }

        val block = event.block
        val location = block.location
        val regions = location.getProtectedRegions(false)

        if (regions.isEmpty()) {
            return
        }

        val canExplode = regions.all { region ->
            region.getFlag(ProtectionFlagsRegistry.TNT_EXPLODE) == StateFlag.State.ALLOW
        }

        event.isCancelled = !canExplode
    }
}