package dev.slne.protect.bukkit.user;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.book.ProtectionBook;
import dev.slne.protect.bukkit.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.transaction.Transaction;
import dev.slne.transaction.api.transaction.data.TransactionData;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @return The {@link ProtectionUser}
     */
    public static ProtectionUser getProtectionUser(OfflinePlayer player) {
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
     * @return the future when the transaction is completed
     */
    public CompletableFuture<TransactionAddResult> addTransaction(@Nullable UUID sender,
                                                                  @NotNull BigDecimal amount,
                                                                  @NotNull Currency currency,
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
     * @return The future when the check is completed
     */
    public CompletableFuture<Boolean> hasEnoughCurrency(BigDecimal amount, Currency currency) {
        return TransactionApi.getTransactionPlayer(uuid).hasEnough(currency, amount);
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
        Player player = getBukkitPlayer();
        if (this.regionCreation != null) {
            player.sendMessage(MessageManager.prefix()
                    .append(Component.text("Du befindest dich bereits im ProtectionMode.", MessageManager.ERROR)));
            return false;
        }

        if (player.getLocation().getWorld().getName().contains("_nether") ||
                player.getLocation().getWorld().getName().contains("_end")) {
            player.sendMessage(MessageManager.prefix()
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

                player.sendMessage(Component.text().append(MessageManager.prefix())
                        .append(Component.text("Du kannst den ProtectionMode erst wieder in ", MessageManager.ERROR))
                        .append(Component.text(time, MessageManager.VARIABLE_VALUE)
                                .append(Component.text(" Minuten verwenden.", MessageManager.ERROR))).build());

                return false;
            }
        }
        creationCooldown.put(player.getName(), System.currentTimeMillis());

        final WorldBorder worldBorder = Bukkit.createWorldBorder();

        Location location = player.getLocation();
        double worldBorderSize = ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START;

        if (regionCreation.isExpandingRegion()) {
            ProtectedRegion expandingProtection = regionCreation.getExpandingProtection();
            Region regionConverter = WorldEditRegionConverter.convertToRegion(expandingProtection);
            BlockVector2 center = regionConverter.getCenter().toBlockPoint().toBlockVector2();
            location = new Location(player.getWorld(), center.getX(), 0, center.getZ());

            worldBorderSize = getWorldBorderSize(expandingProtection, worldBorderSize, center);
            regionCreation.setWorldBorderSize(worldBorderSize);
        }

        worldBorder.setCenter(location.getBlockX(), location.getBlockZ());
        worldBorder.setSize(worldBorderSize);
        worldBorder.setWarningDistance(0);

        player.getInventory().clear();
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCollidable(false);
        player.setWorldBorder(worldBorder);

        PacketEvents.getAPI().getPlayerManager().sendPacketSilently(
                player,
                new WrapperPlayWorldBorderLerpSize(worldBorderSize, worldBorderSize - 0.001, Long.MAX_VALUE)
        );

        Inventory inventory = player.getInventory();
        inventory.setItem(0, ProtectionHotbarListener.markerItem);
        inventory.setItem(7, ProtectionHotbarListener.acceptItem);
        inventory.setItem(8, ProtectionHotbarListener.cancelItem);

        player.openBook(new ProtectionBook().getBook());

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

        Player player = getBukkitPlayer();
        player.setFallDistance(0);
        player.teleportAsync(creation.getStartLocation());

        player.getInventory().setContents(creation.getStartingInventoryContent());

        player.setAllowFlight(player.getGameMode().equals(GameMode.CREATIVE));
        player.setFlying(player.getGameMode().equals(GameMode.CREATIVE));
        player.setCollidable(true);
        player.setWorldBorder(null);

    }

    public double getWorldBorderSize(ProtectedRegion expandingProtection, double worldBorderSize, BlockVector2 center) {
        BlockVector2 furthestPoint = expandingProtection.getPoints().stream().reduce((first, second) -> {
            int firstDistance = first.distanceSq(center);
            int secondDistance = second.distanceSq(center);

            return firstDistance > secondDistance ? first : second;
        }).orElseThrow();
        double distance = furthestPoint.distance(center);
        return worldBorderSize + distance;
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
