package dev.slne.surf.protect.paper.listener.listeners

import com.destroystokyo.paper.event.block.BlockDestroyEvent
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.items.ProtectionItems
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.user.protectionUser
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

object ProtectionHotbarListener : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        val protectionUser = player.protectionUser()
        val regionCreation = protectionUser.regionCreation ?: return
        val clickedItem = event.item ?: return
        val protectionItem = ProtectionItems.getProtectionItem(clickedItem) ?: return

        if (protectionItem.cancelEvent) {
            event.isCancelled = true
        }

        plugin.launch {
            protectionItem.handleInteract(protectionUser, regionCreation, event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onMarkerBreak(event: BlockBreakEvent) {
        val block = event.block
        val player = event.player
        val protectionUser = player.protectionUser()
        val protectionItem = ProtectionItems.getProtectionBlock(block) ?: return

        if (protectionItem.cancelEvent) {
            event.isCancelled = true
        }

        protectionItem.handleBlockBreak(protectionUser, event)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockToFrom(event: BlockFromToEvent) {
        if (ProtectionItems.isProtectionBlock(event.toBlock)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMarkerDestroy(event: BlockDestroyEvent) {
        if (ProtectionItems.isProtectionBlock(event.block)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPistonBreakMarker(event: BlockPistonExtendEvent) {
        if (event.blocks.any { ProtectionItems.isProtectionBlock(it) }) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMarkerEntityExplode(event: EntityExplodeEvent) {
        event.blockList().removeIf { ProtectionItems.isProtectionBlock(it) }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMarkerBlockExplode(event: BlockExplodeEvent) {
        event.blockList().removeIf { ProtectionItems.isProtectionBlock(it) }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onMarkerPlace(event: BlockPlaceEvent) {
        val block = event.getBlock()
        val location = block.getLocation()
        val player = event.getPlayer()
        val protectionUser = player.protectionUser()
        val protectionItem = ProtectionItems.getProtectionItem(event.itemInHand) ?: return

        if (protectionItem.cancelEvent) {
            event.isCancelled = true
        }

        protectionItem.handleBlockPlace(protectionUser, event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.isCancelled) return
        val block = event.blockPlaced
        val protectionItem = ProtectionItems.getProtectionItem(event.itemInHand) ?: return
        ProtectionItems.makeProtectionBlock(protectionItem, block)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.isCancelled) return
        val block = event.block
        ProtectionItems.removeProtectionBlock(block)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBlockDestroy(event: BlockDestroyEvent) {
        if (event.isCancelled) return
        val block = event.block
        ProtectionItems.removeProtectionBlock(block)
    }
}
