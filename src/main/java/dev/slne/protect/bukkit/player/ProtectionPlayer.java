package dev.slne.protect.bukkit.player;

import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.player.inventory.SavedInventory;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * The type Protection player.
 */
public class ProtectionPlayer {

  private static final ComponentLogger LOGGER = ComponentLogger.logger(ProtectionPlayer.class);

  private final UUID uuid;

  private ProtectionRegion currentRegion;

  private Location savedLocation;
  private SavedInventory savedInventory;

  /**
   * Instantiates a new Protection player.
   *
   * @param uuid the uuid
   */
  public ProtectionPlayer(UUID uuid) {
    this.uuid = uuid;
  }

  /**
   * Get protection player.
   *
   * @param uuid the uuid
   * @return the protection player
   */
  public static ProtectionPlayer get(UUID uuid) {
    return ProtectionPlayerManager.INSTANCE.getProtectionPlayer(uuid);
  }

  /**
   * Get protection player.
   *
   * @param player the player
   * @return the protection player
   */
  public static ProtectionPlayer get(OfflinePlayer player) {
    return get(player.getUniqueId());
  }

  /**
   * Toggle region creation.
   *
   * @param creatingRegion the creating region
   */
  public void toggleRegionCreation(boolean creatingRegion) {
    Player player = getPlayer();

    if (player == null) {
      return;
    }

    if (creatingRegion) {
      saveInventory();

      if (currentRegion != null) {
        savedLocation = player.getLocation();

        player.teleportAsync(currentRegion.getTeleportLocation(player.getWorld()))
            .exceptionally(exception -> {
              LOGGER.error("Failed to teleport player to center location", exception);

              player.sendMessage(MessageManager.prefix().append(Component.text(
                  "Es gab einen Fehler bei der Teleportation. Bitte melde dies per Ticket. Danke!",
                  MessageManager.ERROR)));

              return null;
            });
      }
    } else {
      restoreInventory();

      if (savedLocation != null) {
        player.teleportAsync(savedLocation).exceptionally(exception -> {
          LOGGER.error("Failed to teleport player to saved location", exception);

          player.sendMessage(MessageManager.prefix().append(Component.text(
              "Es gab einen Fehler bei der Teleportation. Bitte melde dies per Ticket. Danke!",
              MessageManager.ERROR)));

          return null;
        });
      }
    }

    player.setAllowFlight(
        creatingRegion || player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode()
            .equals(GameMode.SPECTATOR));
    player.setFlying(creatingRegion);
  }

  /**
   * Save inventory.
   */
  public void saveInventory() {
    savedInventory = new SavedInventory(getPlayer().getInventory());
  }

  /**
   * Restore inventory.
   */
  public void restoreInventory() {
    if (savedInventory == null) {
      return;
    }

    Player player = getPlayer();

    if (player == null || !player.isOnline()) {
      return;
    }

    player.getInventory().setArmorContents(savedInventory.getArmorContents());
    player.getInventory().setContents(savedInventory.getInventoryContents());
    player.getInventory().setExtraContents(savedInventory.getExtraContents());
  }

  /**
   * Gets offline player.
   *
   * @return the offline player
   */
  public OfflinePlayer getOfflinePlayer() {
    return Bukkit.getOfflinePlayer(uuid);
  }

  /**
   * Gets player.
   *
   * @return the player
   */
  public Player getPlayer() {
    return Bukkit.getPlayer(uuid);
  }

  /**
   * Gets uuid.
   *
   * @return the uuid
   */
  public UUID getUuid() {
    return uuid;
  }

  /**
   * Gets saved inventory.
   *
   * @return the saved inventory
   */
  public SavedInventory getSavedInventory() {
    return savedInventory;
  }

  /**
   * Sets saved inventory.
   *
   * @param savedInventory the saved inventory
   */
  public void setSavedInventory(SavedInventory savedInventory) {
    this.savedInventory = savedInventory;
  }

  /**
   * Gets current region.
   *
   * @return the current region
   */
  public ProtectionRegion getCurrentRegion() {
    return currentRegion;
  }

  /**
   * Sets current region.
   *
   * @param currentRegion the current region
   */
  public void setCurrentRegion(ProtectionRegion currentRegion) {
    this.currentRegion = currentRegion;
  }

  /**
   * Gets saved location.
   *
   * @return the saved location
   */
  public Location getSavedLocation() {
    return savedLocation;
  }

  /**
   * Sets saved location.
   *
   * @param savedLocation the saved location
   */
  public void setSavedLocation(Location savedLocation) {
    this.savedLocation = savedLocation;
  }

  /**
   * Has current region boolean.
   *
   * @return the boolean
   */
  public boolean hasCurrentRegion() {
    return currentRegion != null;
  }
}
