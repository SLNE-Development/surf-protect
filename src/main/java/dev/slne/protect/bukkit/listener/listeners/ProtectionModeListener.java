package dev.slne.protect.bukkit.listener.listeners;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginDisableEvent;

public class ProtectionModeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

        if (protectionUser.hasRegionCreation()) {
            protectionUser.getRegionCreation().cancelProtection();
            protectionUser.resetRegionCreation();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
        ProtectionRegion regionCreation = protectionUser.getRegionCreation();

        if (regionCreation != null) {
            Location protectionModeLocation = regionCreation.getStartLocation();

            Location to = event.getTo();
            if (to.distanceSquared(protectionModeLocation) > (ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START_SQUARED + 2)) {
                event.getPlayer().teleport(protectionModeLocation);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortal(PlayerPortalEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityAttack(PrePlayerAttackEntityEvent event) {
        Player player = event.getPlayer();
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }

        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (protectionUser.hasRegionCreation()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerStop(PluginDisableEvent event) { // TODO: 04.02.2024 11:10 - move to a better place
        for (ProtectionUser protectionUser : BukkitMain.getBukkitInstance().getUserManager().getUsers()) {
            if (protectionUser.hasRegionCreation()) {
                protectionUser.getRegionCreation().cancelProtection();
            }
        }
    }
}
