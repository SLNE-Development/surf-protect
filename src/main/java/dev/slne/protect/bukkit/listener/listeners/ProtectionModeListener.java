package dev.slne.protect.bukkit.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.util.Vector;

import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionCreation;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;

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
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			int maxRange = 100, teleportMaxRange = maxRange + 20;
			float throwbackForce = .2f;

			Player player = event.getPlayer();
			Location playerLocation = player.getLocation();
			Location protectionModeLocation = regionCreation.getStartLocation();

			double currentDistance = playerLocation.distance(protectionModeLocation);

			if (currentDistance >= maxRange && currentDistance <= teleportMaxRange) {
				throwbackPlayer(player, throwbackForce, protectionModeLocation);
				protectionUser.getBukkitPlayer()
						.sendMessage(Component.text().append(MessageManager.prefix())
								.append(Component.text(
										"Du darfst dich nicht weiter von deinem Ausgangspunkt entfernen.",
										MessageManager.ERROR))
								.build());
			} else if (currentDistance > teleportMaxRange) {
				player.teleport(protectionModeLocation);
			}
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPortal(PlayerPortalEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		RegionCreation regionCreation = protectionUser.getRegionCreation();

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
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemPickup(PlayerAttemptPickupItemEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHandSwap(PlayerSwapHandItemsEvent event) {
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			ProtectionUser protectionUser = ProtectionUser.getProtectionUser((Player) event.getDamager());
			RegionCreation regionCreation = protectionUser.getRegionCreation();

			if (regionCreation != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (!(event.getTarget() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getTarget();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	private void throwbackPlayer(Player player, float force, Location protectionModeLocation) {
		Location playerLocation = player.getLocation();

		Vector playerLocationVector = playerLocation.toVector();
		Vector protectionModeLocationVector = protectionModeLocation.toVector();
		Vector throwbackVector = protectionModeLocationVector.subtract(playerLocationVector).multiply(force).setY(0);

		player.playSound(playerLocation, Sound.ENTITY_ENDER_DRAGON_FLAP, 3, 2);
		player.setVelocity(throwbackVector);
	}
}
