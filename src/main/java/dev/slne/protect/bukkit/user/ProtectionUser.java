package dev.slne.protect.bukkit.user;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.book.ProtectionBook;
import dev.slne.protect.bukkit.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.transaction.Transaction;
import dev.slne.transaction.api.transaction.data.TransactionData;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ProtectionUser {

    private final HashMap<String, Long> creationCooldown;
    private final UUID uuid;
    private ProtectionRegion regionCreation;
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
     *
     * @return The {@link ProtectionUser}
     */
    public static ProtectionUser getProtectionUser(OfflinePlayer player) {
        return getProtectionUser(player.getUniqueId());
    }

    /**
     * Returns the {@link ProtectionUser} for the given {@link UUID}
     *
     * @param uuid The UUID of the user
     *
     * @return The {@link ProtectionUser}
     */
    public static ProtectionUser getProtectionUser(UUID uuid) {
        return BukkitMain.getBukkitInstance().getUserManager().getProtectionUser(uuid);
    }

    /**
     * Applies the {@link LocalPlayer} to the user
     */
    private void applyLocalPlayer() {
        if (Bukkit.getPlayer(uuid) != null && Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
            this.localPlayer = WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid));
        } else {
            this.localPlayer = WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
        }

    }

    /**
     * Adds a transaction to the user
     *
     * @param sender   The sender of the transaction
     * @param amount   The amount of the transaction
     * @param currency The currency of the transaction
     * @param data     The data of the transaction
     *
     * @return the future when the transaction is completed
     */
    public CompletableFuture<TransactionAddResult> addTransaction(UUID sender, BigDecimal amount, Currency currency,
                                                                  TransactionData data) {
        Transaction transaction = TransactionApi.createTransaction(sender, uuid, currency, amount);
        transaction.setTransactionData(data);

        return TransactionApi.getTransactionPlayer(uuid).addTransaction(transaction);
    }

    /**
     * Checks if the user has enough currency
     *
     * @param amount   The amount to check
     * @param currency The currency to check
     *
     * @return The future when the check is completed
     */
    public CompletableFuture<Boolean> hasEnoughCurrency(BigDecimal amount, Currency currency) {
        return TransactionApi.getTransactionPlayer(uuid).getBalance(currency)
                .thenApplyAsync(sum -> sum.subtract(amount).compareTo(BigDecimal.ZERO) >= 0);
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
     * Returns the {@link ProtectionRegion} the user is currently creating
     *
     * @return The {@link ProtectionRegion} the user is currently creating
     */
    public ProtectionRegion getRegionCreation() {
        return regionCreation;
    }

    /**
     * Starts the creation of a {@link ProtectionRegion}
     *
     * @param regionCreation The {@link ProtectionRegion} to create
     */
    public boolean startRegionCreation(ProtectionRegion regionCreation) {
        if (this.regionCreation != null) {
            getBukkitPlayer().sendMessage(MessageManager.prefix()
                    .append(Component.text("Du befindest dich bereits im ProtectionMode.", MessageManager.ERROR)));
            return false;
        }

        if (getBukkitPlayer().getLocation().getWorld().getName().contains("_nether") ||
                getBukkitPlayer().getLocation().getWorld().getName().contains("_end")) {
            getBukkitPlayer().sendMessage(MessageManager.prefix()
                    .append(Component.text("Du befindest dich nicht in der Overworld.", MessageManager.ERROR)));
            return false;
        }

        int cooldownTime = ProtectionSettings.REGION_CREATION_COOLDOWN;
        if (creationCooldown.containsKey(localPlayer.getName())) {
            long secondsLeft =
                    ((creationCooldown.get(localPlayer.getName()) / 1000) + cooldownTime) -
                            (System.currentTimeMillis() / 1000);

            if (secondsLeft > 0) {
                String time = String.format("%1$2d:%2$2d", secondsLeft / 60, secondsLeft % 60).replace(" ", "0");

                getBukkitPlayer().sendMessage(Component.text().append(MessageManager.prefix())
                        .append(Component.text("Du kannst den ProtectionMode erst wieder in ", MessageManager.ERROR))
                        .append(Component.text(time, MessageManager.VARIABLE_VALUE)
                                .append(Component.text(" Minuten verwenden.", MessageManager.ERROR))).build());

                return false;
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

        getBukkitPlayer().openBook(new ProtectionBook().getBook());

        this.regionCreation = regionCreation;

        return true;
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
     * Resets the creation of a {@link ProtectionRegion}
     */
    public void resetRegionCreation() {
        if (this.regionCreation == null) {
            return;
        }

        ProtectionRegion creation = regionCreation;
        regionCreation = null;

        getBukkitPlayer().setFallDistance(0);
        getBukkitPlayer().teleport(creation.getStartLocation());

        getBukkitPlayer().getInventory().setContents(creation.getStartingInventoryContent());

        getBukkitPlayer().setAllowFlight(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
        getBukkitPlayer().setFlying(getBukkitPlayer().getGameMode().equals(GameMode.CREATIVE));
        getBukkitPlayer().setCollidable(true);

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
     * Returns the {@link UUID} of the user
     *
     * @return The {@link UUID}
     */
    public UUID getUuid() {
        return uuid;
    }

}
