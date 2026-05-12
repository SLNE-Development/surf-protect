package dev.slne.surf.protect.paper.listener.listeners

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.util.getProtectedRegions
import dev.slne.surf.protect.paper.util.isGlobalRegion
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.Location
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntitySpawnEvent


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

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockForm(event: BlockFormEvent) {
        val block = event.block
        if (!Tag.CONCRETE_POWDER.isTagged(block.type)) return

        if (block.location.isGlobalRegion()) {
            return  // Allow
        }

        val regions = block.location.getProtectedRegions()
        for (region in regions) {
            val flagState = region.getFlag(ProtectionFlagsRegistry.CONCRETE_FORM) ?: StateFlag.State.ALLOW
            if (flagState == StateFlag.State.DENY) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun handleFallingBlockChange(event: EntityChangeBlockEvent) {
        if (event.entity !is FallingBlock) {
            return
        }

        val blockBelow = event.block.getRelative(0, -1, 0)
        if (!blockBelow.isPassable) {
            return
        }

        val gravityDenied = gravityDeniedAt(event.block.location)
        if (gravityDenied) {
            event.isCancelled = true
        }
    }

    private fun gravityDeniedAt(location: Location): Boolean {
        return location.getProtectedRegions().any {
            (it.getFlag(ProtectionFlagsRegistry.SURF_BLOCK_GRAVITY) ?: StateFlag.State.ALLOW) == StateFlag.State.DENY
        }
    }

    private fun performBlockRemove(blocks: MutableList<Block>) {
        blocks.removeIf { block ->
            block.location.getProtectedRegions().any {
                (it.getFlag(Flags.OTHER_EXPLOSION) ?: StateFlag.State.DENY) == StateFlag.State.DENY
            }
        }
    }
}
