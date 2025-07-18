package dev.slne.surf.protect.paper.listener.listeners

import com.destroystokyo.paper.MaterialSetTag
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.protect.paper.items.ProtectionItems
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.user.protectionUser
import dev.slne.surf.surfapi.bukkit.api.util.key
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.player.*

object ProtectionModeListener : Listener {
    private fun Block.isInteractive() = INTERACTABLE.isTagged(this)

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent) {
        val protectionUser = event.player.protectionUser()

        val regionCreation = protectionUser.regionCreation
        if (regionCreation != null) { // TODO: 09.07.2025 23:46 - does this actually work?
            protectionUser.restorePlayerProperties(event.player, regionCreation)
            plugin.launch { regionCreation.cancelProtection() }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onMove(event: PlayerMoveEvent) {
        if (!(event.hasExplicitlyChangedBlock())) return
        val player = event.player
        val protectionUser = player.protectionUser()
        val regionCreation = protectionUser.regionCreation

        if (regionCreation != null) {
            val protectionModeLocation = regionCreation.startLocation
            val worldBorder = player.worldBorder

            if (worldBorder != null && !worldBorder.isInside(event.to)) {
                player.teleportAsync(protectionModeLocation)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        if (event.to.world != event.from.world && event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemDrop(event: PlayerDropItemEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPortal(event: PlayerPortalEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemPickup(event: PlayerAttemptPickupItemEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onHandSwap(event: PlayerSwapHandItemsEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityAttack(event: PrePlayerAttackEntityEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityTarget(event: EntityTargetEvent) {
        val player = event.target as? Player ?: return
        if (player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        if (!player.protectionUser().isCreatingRegion) return
        val block = event.clickedBlock
        val isInteracting = block?.isInteractive() ?: false

        // Check if the player is interacting with a block instead of placing a redstone torch and if so cancel the event
        if (isInteracting && !player.isSneaking) {
            event.setCancelled(true)
            return
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!event.player.protectionUser().isCreatingRegion) return
        if (!(ProtectionItems.isProtectionBlock(event.block))) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!event.player.protectionUser().isCreatingRegion) return
        if (!(ProtectionItems.isProtectionItem(event.itemInHand))) {
            event.isCancelled = true
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    fun onItemFrameChange(event: PlayerItemFrameChangeEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onArmorstandManipulate(event: PlayerArmorStandManipulateEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityRide(event: PlayerInteractEntityEvent) {
        if (event.player.protectionUser().isCreatingRegion) {
            event.isCancelled = true
        }
    }

    private val INTERACTABLE = MaterialSetTag(key("interactable"))
        .contains("BUTTON")
        .contains("DOOR")
        .contains("LEVER")
        .contains("GATE")
        .contains("TRAPDOOR")
        .contains("ANVIL")
        .contains("TABLE")
        .contains("FURNACE")
        .contains("SMOKER")
        .contains("BREWING_STAND")
        .contains("LECTERN")
        .contains("STONECUTTER")
        .contains("GRINDSTONE")
        .contains("HOPPER")
        .contains("DROPPER")
        .contains("DISPENSER")
        .contains("CHEST")
        .contains("SHULKER_BOX")
        .contains("BARREL")
        .contains("BEACON")
        .contains("BED")
        .contains("CAULDRON")
        .contains("COMPARATOR")
        .contains("DAYLIGHT_DETECTOR")
        .contains("SIGN")
        .contains("PRESSURE_PLATE")
        .contains("JUKEBOX")
        .contains("NOTE_BLOCK")
        .contains("BELL")
        .contains("CAMPFIRE")
        .contains("POT")
        .add(Material.LOOM)
        .add(Material.REPEATER)
        .add(Material.OBSERVER)
        .add(Material.CHISELED_BOOKSHELF)
        .lock()
}
