package dev.slne.protect.bukkit.gui;

import java.util.concurrent.ExecutionException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.regions.RegionCreation;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.utils.ItemStackUtils;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import dev.slne.protect.bukkit.utils.ProtectionUtils;
import dev.slne.protect.bukkit.visual.Marker;

public class ProtectionHotbarGui implements Listener {

	public static ItemStack markerItem = ItemStackUtils.getItem(Material.REDSTONE_TORCH, ProtectionSettings.MARKERS, 0,
			"§eMarker", "§7Platziere die Marker um dein Grundstück zu definieren");
	public static ItemStack acceptItem = ItemStackUtils.getItem(Material.LIME_CONCRETE, 1, 0, "§aKaufen",
			"§7Bestätige deine Auswahl und erwerbe das Grundstück");
	public static ItemStack cancelItem = ItemStackUtils.getItem(Material.RED_CONCRETE, 1, 0, "§cAbbrechen",
			"§7Verlasse den ProtectionMode und kehre zum normalen Spiel zurück");

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

		RegionCreation regionCreation = protectionUser.getRegionCreation();

		if (regionCreation == null) {
			return;
		}

		ItemStack clickedItem = event.getItem();
		if (clickedItem == null || clickedItem.getItemMeta() == null || !clickedItem.getItemMeta().hasDisplayName()) {
			return;
		}

		if (clickedItem.getItemMeta().displayName().equals(acceptItem.getItemMeta().displayName())) {
			try {
				regionCreation.finishProtection();
				event.setCancelled(true);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		} else if (clickedItem.getItemMeta().displayName().equals(cancelItem.getItemMeta().displayName())) {
			regionCreation.cancelProtection();
			event.setCancelled(true);
		}
	}

	public void setMarkerAmount(ProtectionUser protectionUser) {
		RegionCreation regionCreation = protectionUser.getRegionCreation();
		ItemStack newMarkerItem = markerItem.clone();
		newMarkerItem.setAmount(regionCreation.getMarkerCountLeft());

		protectionUser.getBukkitPlayer().getInventory().setItem(0, newMarkerItem);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onMarkerBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

		if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
			Object obj = block.getState().getMetadata(ProtectionSettings.MARKER_KEY).get(0).value();

			if (obj instanceof Marker) {
				Marker marker = (Marker) obj;
				RegionCreation creation = marker.getRegionCreation();
				if (protectionUser.getRegionCreation() != creation) {
					event.setCancelled(true);
					return;
				}

				creation.removeMarker(marker, true);
				block.getState().removeMetadata(ProtectionSettings.MARKER_KEY, BukkitMain.getInstance());

				event.setDropItems(false);
				this.setMarkerAmount(protectionUser);
			}

			return;
		}

		if (event.getPlayer() == null) {
			return;
		}

		if (protectionUser.getRegionCreation() != null) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlockToFrom(BlockFromToEvent event) {
		Block block = event.getToBlock();
		if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMarkerPhysics(BlockPhysicsEvent event) {
		Location root = event.getBlock().getLocation();

		for (Vector vector : new Vector[] { new Vector(0, 1, 0), // up
				new Vector(1, 0, 0), // north
				new Vector(0, 0, 1), // east
				new Vector(-1, 0, 0), // south
				new Vector(0, 0, -1) // west
		}) {
			Location location = root.clone().add(vector);
			Block block = location.getBlock();
			if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onMarkerPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		Location location = block.getLocation();
		Player player = event.getPlayer();
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

		if (ProtectionUtils.isInProtectionRegion(location)) {
			ItemStack clickedItem = event.getItemInHand();

			if (clickedItem.getItemMeta() == null || !clickedItem.getItemMeta().hasDisplayName()) {
				return;
			}

			if (clickedItem.getItemMeta().displayName().equals(markerItem.getItemMeta().displayName())) {
				RegionCreation regionCreation = protectionUser.getRegionCreation();

				if (regionCreation == null) {
					event.setCancelled(true);
					return;
				}

				// todo: place checks
				BlockData replaced = event.getBlockReplacedState().getBlockData();

				final Marker marker = regionCreation.createMarker(event.getBlock(), replaced);
				if (marker == null) {
					event.setCancelled(true);
					return;
				}

				location.getBlock().getState().setMetadata(ProtectionSettings.MARKER_KEY,
						new FixedMetadataValue(BukkitMain.getInstance(), marker));
				setMarkerAmount(protectionUser);
				return;
			}
		}

		if (event.getPlayer() == null) {
			return;
		}

		if (protectionUser.getRegionCreation() != null) {
			event.setCancelled(true);
			return;
		}
	}

}
