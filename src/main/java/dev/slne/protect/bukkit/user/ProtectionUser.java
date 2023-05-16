package dev.slne.protect.bukkit.user;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.ProtectionHotbarGui;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionCreation;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import net.kyori.adventure.text.Component;

public class ProtectionUser {

	private RegionCreation regionCreation;
	private HashMap<String, Long> creationCooldown;

	private UUID uuid;
	private LocalPlayer localPlayer;

	/**
	 * Construct a new user
	 * 
	 * @param uuid The UUID of the user
	 */
	public ProtectionUser(UUID uuid) {
		this.uuid = uuid;
		this.creationCooldown = new HashMap<String, Long>();

		applyLocalPlayer();
	}

	/**
	 * Returns the {@link ProtectionUser} for the given {@link Player}
	 * 
	 * @param player The player
	 * @return The {@link ProtectionUser}
	 */
	public static ProtectionUser getProtectionUser(Player player) {
		return getProtectionUser(player.getUniqueId());
	}

	/**
	 * Returns the {@link ProtectionUser} for the given {@link UUID}
	 * 
	 * @param uuid The UUID of the user
	 * @return The {@link ProtectionUser}
	 */
	public static ProtectionUser getProtectionUser(UUID uuid) {
		return BukkitMain.getBukkitInstance().getUserManager().getProtectionUser(uuid);
	}

	public RegionCreation getRegionCreation() {
		return regionCreation;
	}

	public boolean hasRegionCreation() {
		return this.getRegionCreation() != null;
	}

	public void startRegionCreation(RegionCreation regionCreation) {
		if (this.regionCreation != null) {
			getBukkitPlayer().sendMessage(MessageManager.prefix()
					.append(Component.text("Du befindest dich bereits im ProtectionMode.", MessageManager.ERROR)));
			return;
		}

		if (getBukkitPlayer().getLocation().getWorld().getName().contains("_nether")
				|| getBukkitPlayer().getLocation().getWorld().getName().contains("_end")) {
			getBukkitPlayer().sendMessage(MessageManager.prefix()
					.append(Component.text("Du befindest dich nicht in der Overworld.", MessageManager.ERROR)));
			return;
		}

		int cooldownTime = ProtectionSettings.REGION_CREATION_COOLDOWN;
		if (creationCooldown.containsKey(localPlayer.getName())) {
			long secondsLeft = ((creationCooldown.get(localPlayer.getName()) / 1000) + cooldownTime)
					- (System.currentTimeMillis() / 1000);

			if (secondsLeft > 0) {

				String time = String.format("%1$2d:%2$2d", secondsLeft / 60, secondsLeft % 60).replace(" ", "0");
				getBukkitPlayer().sendMessage(Component.text().append(MessageManager.prefix())
						.append(Component.text("Du kannst den ProtectionMode erst wieder in ", MessageManager.ERROR))
						.append(Component.text(time, MessageManager.VARIABLE_VALUE)
								.append(Component.text(" Minuten verwenden.", MessageManager.ERROR)))
						.build());
				return;
			}
		}
		creationCooldown.put(getBukkitPlayer().getName(), System.currentTimeMillis());

		getBukkitPlayer().getInventory().clear();
		getBukkitPlayer().setAllowFlight(true);
		getBukkitPlayer().setFlying(true);
		getBukkitPlayer().setCollidable(false);

		Inventory inventory = getBukkitPlayer().getInventory();
		inventory.setItem(0, ProtectionHotbarGui.markerItem);
		inventory.setItem(7, ProtectionHotbarGui.acceptItem);
		inventory.setItem(8, ProtectionHotbarGui.cancelItem);

		this.regionCreation = regionCreation;
	}

	public void resetRegionCreation() {
		if (this.regionCreation == null) {
			return;
		}

		RegionCreation creation = this.regionCreation;
		this.regionCreation = null;
		getBukkitPlayer().teleport(creation.getStartLocation());

		getBukkitPlayer().getInventory().setContents(creation.getStartingInventoryContent());

		getBukkitPlayer().setAllowFlight(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
		getBukkitPlayer().setFlying(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
		getBukkitPlayer().setCollidable(true);
	}

	private LocalPlayer applyLocalPlayer() {
		if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
			return this.localPlayer = WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid));
		}

		return this.localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
	}

	public void sendMessage(Component message) {
		getBukkitPlayer().sendMessage(message);
	}

	public LocalPlayer getLocalPlayer() {
		return localPlayer;
	}

	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public UUID getUuid() {
		return uuid;
	}

}
