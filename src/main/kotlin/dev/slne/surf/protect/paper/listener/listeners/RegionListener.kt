package dev.slne.surf.protect.paper.listener.listeners

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.util.getProtectedRegions
import dev.slne.surf.protect.paper.util.isGlobalRegion
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause
import org.bukkit.event.entity.EntityExplodeEvent

object RegionListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onIgnite(event: BlockIgniteEvent) {
        val cause = event.cause
        if (cause == IgniteCause.FLINT_AND_STEEL || cause == IgniteCause.FIREBALL) {
            return  // Allow
        }

        if (event.block.location.isGlobalRegion()) {
            return  // Allow
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityExplode(event: EntityExplodeEvent) {
        performBlockRemove(event.blockList())
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockExplode(event: BlockExplodeEvent) {
        performBlockRemove(event.blockList())
    }

    private fun performBlockRemove(blocks: MutableList<Block>) {
        blocks.removeIf { block ->
            block.location.getProtectedRegions().any {
                (it.getFlag(Flags.OTHER_EXPLOSION) ?: StateFlag.State.DENY) == StateFlag.State.DENY
            }
        }
    }
}
