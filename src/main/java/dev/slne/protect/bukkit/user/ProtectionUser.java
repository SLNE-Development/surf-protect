package dev.slne.protect.bukkit.user;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import dev.slne.data.core.database.future.SurfFutureResult;
import dev.slne.data.core.instance.DataApi;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.transaction.core.currency.Currency;
import dev.slne.transaction.core.player.TransactionPlayer;
import dev.slne.transaction.core.transaction.Transaction;
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
	 * @param sender   The sender of the transaction
	 * @param amount   The amount of the transaction
	 * @param currency The currency of the transaction
	 * @return the future when the transaction is completed
	 */
	public SurfFutureResult<Optional<Boolean>> addTransaction(UUID sender, BigDecimal amount, Currency currency) {
		TransactionPlayer player = TransactionPlayer.getTransactionPlayer(uuid);
		Transaction transaction = new Transaction(sender, uuid, amount, currency);

		return player.asyncAddTransaction(transaction);
	}

	/**
	 * Checks if the user has enough currency
	 *
	 * @param amount   The amount to check
	 * @param currency The currency to check
	 * @return The future when the check is completed
	 */
	public SurfFutureResult<Boolean> hasEnoughCurrency(BigDecimal amount, Currency currency) {
		return DataApi.getDataInstance().supplyAsync(() -> {

			TransactionPlayer player = TransactionPlayer.getTransactionPlayer(uuid);
			BigDecimal sum = player.sumTransactions(currency);

			BigDecimal result = sum.subtract(amount);
			return result.compareTo(BigDecimal.ZERO) >= 0;
		});
	}

	/**
	 * Returns the {@link ProtectionRegion} the user is currently creating
	 *
	 * @return The {@link ProtectionRegion} the user is currently creating
	 */
	public ProtectionRegion getRegionCreation() {
		return regionCreation;
	}

	/**
	 * Checks if the user is currently creating a {@link ProtectionRegion}
	 *
	 * @return True if the user is currently creating a
	 */
	public boolean hasRegionCreation() {
		return this.getRegionCreation() != null;
	}

	/**
	 * Starts the creation of a {@link ProtectionRegion}
	 *
	 * @param regionCreation The {@link ProtectionRegion} to create
	 */
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

	/**
	 * Resets the creation of a {@link ProtectionRegion}
	 */
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

	/**
	 * Applies the {@link LocalPlayer} to the user
	 *
	 * @return The {@link LocalPlayer}
	 */
	private LocalPlayer applyLocalPlayer() {
		if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
			this.localPlayer = WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid));
		} else {
			this.localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
		}

		return this.localPlayer;
	}

	/**
	 * Sends a message to the user
	 *
	 * @param message The message to send
	 */
	public void sendMessage(Component message) {
		getBukkitPlayer().sendMessage(message);
	}

	/**
	 * Returns the {@link LocalPlayer} of the user
	 *
	 * @return The {@link LocalPlayer}
	 */
	public LocalPlayer getLocalPlayer() {
		return localPlayer;
	}

	/**
	 * Returns the {@link Player} of the user
	 *
	 * @return The {@link Player}
	 */
	public Player getBukkitPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	/**
	 * Returns the {@link UUID} of the user
	 *
	 * @return The {@link UUID}
	 */
	public UUID getUuid() {
		return uuid;
	}

}
