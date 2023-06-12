package dev.slne.protect.bukkit.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.util.Vector;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

public class ProtectionModeListener implements Listener {

	@EventHandler
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
			Player player = event.getPlayer();
			Location protectionModeLocation = regionCreation.getStartLocation();

			throwbackPlayer(player, protectionModeLocation);
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPortal(PlayerPortalEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null || protectionUser.hasRegionCreation()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemPickup(PlayerAttemptPickupItemEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHandSwap(PlayerSwapHandItemsEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityAttack(PrePlayerAttackEntityEvent event) {
		Player player = event.getPlayer();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (!(event.getTarget() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getTarget();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
		ProtectionRegion regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onServerStop(PluginDisableEvent event) {
		for (ProtectionUser protectionUser : BukkitMain.getBukkitInstance().getUserManager().getUsers()) {
			if (protectionUser.hasRegionCreation()) {
				protectionUser.getRegionCreation().cancelProtection();
			}
		}
	}

	/**
	 * Throwback the player with a given force
	 */
	private void throwbackPlayer(Player player, Location protectionModeLocation) {
		int maxRange = ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START;
		int teleportMaxRange = ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START_TELEPORT;
		double throwbackForce = ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START_FORCE;

		Location playerLocation = player.getLocation();
		Location playerLocationYZero = playerLocation.clone();
		playerLocationYZero.setY(0); // Ignore Y
		Location protectionModeLocationYZero = protectionModeLocation.clone();
		protectionModeLocationYZero.setY(0); // Ignore Y

		Vector playerLocationVector = playerLocation.toVector();
		Vector protectionModeLocationVector = protectionModeLocation.toVector();
		Vector throwbackVector = protectionModeLocationVector.subtract(playerLocationVector).multiply(
				throwbackForce).setY(0);

		double currentDistance = playerLocationYZero.distanceSquared(protectionModeLocationYZero);

		if (currentDistance >= maxRange && currentDistance < teleportMaxRange) {
			player.setVelocity(throwbackVector);
		} else if (currentDistance >= teleportMaxRange) {
			player.teleport(protectionModeLocation);
		} else {
			return;
		}

		player.sendMessage(MessageManager.getTooFarAwayFromStartComponent());
		player.playSound(playerLocation, Sound.ENTITY_ENDER_DRAGON_FLAP, .75f, 2);
	}
}
