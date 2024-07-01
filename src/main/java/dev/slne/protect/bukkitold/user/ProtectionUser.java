package dev.slne.protect.bukkitold.user;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import dev.slne.protect.bukkitold.BukkitMain;
import dev.slne.protect.bukkitold.book.ProtectionBook;
import dev.slne.protect.bukkitold.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkitold.math.Mth;
import dev.slne.protect.bukkitold.message.MessageManager;
import dev.slne.protect.bukkitold.region.ProtectionRegion;
import dev.slne.protect.bukkitold.region.settings.ProtectionSettings;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.transaction.Transaction;
import dev.slne.transaction.api.transaction.data.TransactionData;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    if (Bukkit.getPlayer(uuid) != null && Objects.requireNonNull(Bukkit.getPlayer(uuid))
        .isOnline()) {
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
          .append(Component.text("Du befindest dich bereits im ProtectionMode.",
              MessageManager.ERROR)));
      return false;
    }

    if (player.getLocation().getWorld().getName().contains("_nether") ||
        player.getLocation().getWorld().getName().contains("_end")) {
      player.sendMessage(MessageManager.prefix()
          .append(
              Component.text("Du befindest dich nicht in der Overworld.", MessageManager.ERROR)));
      return false;
    }

    if (creationCooldown.containsKey(localPlayer.getName())) {
      long secondsLeft = Mth.calculateSecondsLeft(creationCooldown.get(localPlayer.getName()),
          ProtectionSettings.REGION_CREATION_COOLDOWN);

      if (secondsLeft > 0) {
        String time = String.format("%1$2d:%2$2d", secondsLeft / 60, secondsLeft % 60)
            .replace(" ", "0");

        player.sendMessage(Component.text().append(MessageManager.prefix())
            .append(Component.text("Du kannst den ProtectionMode erst wieder in ",
                MessageManager.ERROR))
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
      location = new Location(player.getWorld(), center.x(), 0, center.z());

      worldBorderSize = getWorldBorderSize(expandingProtection, worldBorderSize, center);
      regionCreation.setWorldBorderSize(worldBorderSize);
    }

    worldBorder.setCenter(location.getBlockX(), location.getBlockZ());
    worldBorderSize = worldBorderSize
        * 2; // We want to set the diameter not the radius so we need to multiply it by 2. Doing it directly in the set method didn't work for some reason
    worldBorder.setSize(worldBorderSize);
    worldBorder.setWarningDistance(0);

    player.getInventory().clear();
    player.setAllowFlight(true);
    player.setFlying(true);
    player.setFlySpeed(0.3f);
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
    player.teleport(creation.getStartLocation());

    player.getInventory().setContents(creation.getStartingInventoryContent());

    player.setAllowFlight(player.getGameMode().equals(GameMode.CREATIVE));
    player.setFlying(player.getGameMode().equals(GameMode.CREATIVE));
    player.setFlySpeed(0.2f);
    player.setCollidable(true);
    player.setWorldBorder(null);

  }

  public double getWorldBorderSize(ProtectedRegion expandingProtection, double worldBorderSize,
      BlockVector2 center) {
    BlockVector2 furthestPoint = expandingProtection.getPoints().stream()
        .max(Comparator.comparingDouble(point -> point.distance(center))).orElseThrow();

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
