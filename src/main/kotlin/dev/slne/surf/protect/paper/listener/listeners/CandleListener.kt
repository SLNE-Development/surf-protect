package dev.slne.surf.protect.paper.listener.listeners

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.bukkit.cause.Cause
import com.sk89q.worldguard.bukkit.event.DelegateEvent
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent
import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.util.regionContainer
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

object CandleListener : Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlaceCandle(event: PlaceBlockEvent) {
        if (!isCandle(event.effectiveMaterial)) return
        allowIfFlagged(event, event.cause, event.blocks)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onBreakCandle(event: BreakBlockEvent) {
        if (!areCandles(event.blocks)) return
        allowIfFlagged(event, event.cause, event.blocks)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onUseCandle(event: UseBlockEvent) {
        if (!areCandles(event.blocks)) return
        allowIfFlagged(event, event.cause, event.blocks)
    }

    private fun allowIfFlagged(event: DelegateEvent, cause: Cause, blocks: List<Block>) {
        if (blocks.isEmpty()) return

        val player = cause.firstPlayer ?: return
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        val query = regionContainer.createQuery()

        val allowed = blocks.all { block ->
            query.testState(
                BukkitAdapter.adapt(block.location),
                localPlayer,
                ProtectionFlagsRegistry.SURF_CANDLE_INTERACT
            )
        }

        if (allowed) {
            event.setAllowed(true)
        }
    }

    private fun areCandles(blocks: List<Block>) =
        blocks.isNotEmpty() && blocks.all { isCandle(it.type) }

    private fun isCandle(material: Material) = Tag.CANDLES.isTagged(material)
}
