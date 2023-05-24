package dev.slne.protect.bukkit.user;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import net.kyori.adventure.text.Component;

public class ProtectionUser {

	private ProtectionRegion regionCreation;
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
		this.creationCooldown = new HashMap<>();

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

	/**
	 * Adds a transaction to the user
	 *
	 * @param amount the amount to add
	 * @return the future when the transaction is completed
	 */
	public CompletableFuture<Boolean> addTransaction(double amount) {
		return CompletableFuture.completedFuture(true);
	}

	/**
	 * Checks if the user has enough currency
	 *
	 * @param amount the amount to check
	 * @return the future when the check is completed
	 */
	public CompletableFuture<Boolean> hasEnoughCurrency(double amount) {
		return CompletableFuture.completedFuture(true);
	}

	public ProtectionRegion getRegionCreation() {
		return regionCreation;
	}

	public boolean hasRegionCreation() {
		return this.getRegionCreation() != null;
	}

	public void startRegionCreation(ProtectionRegion regionCreation) {
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
		inventory.setItem(0, ProtectionHotbarListener.markerItem);
		inventory.setItem(7, ProtectionHotbarListener.acceptItem);
		inventory.setItem(8, ProtectionHotbarListener.cancelItem);

		this.regionCreation = regionCreation;
	}

	public void resetRegionCreation() {
		if (this.regionCreation == null) {
			return;
		}

		ProtectionRegion creation = this.regionCreation;
		this.regionCreation = null;
		getBukkitPlayer().teleport(creation.getStartLocation());

		getBukkitPlayer().getInventory().setContents(creation.getStartingInventoryContent());

		getBukkitPlayer().setAllowFlight(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
		getBukkitPlayer().setFlying(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
		getBukkitPlayer().setCollidable(true);
	}

	private LocalPlayer applyLocalPlayer() {
		if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
			this.localPlayer = WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid));
		} else {
			this.localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
		}

		return this.localPlayer;
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
