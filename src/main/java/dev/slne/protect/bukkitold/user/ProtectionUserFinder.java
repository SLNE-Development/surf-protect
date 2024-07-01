package dev.slne.protect.bukkitold.user;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utils for {@link LocalPlayer}s
 */
public class ProtectionUserFinder {

  /**
   * Utility class
   */
  private ProtectionUserFinder() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Find a user by their UUID
   *
   * @param uuid The UUID of the user
   * @return The user
   */
  public static LocalPlayer findLocalPlayer(UUID uuid) {
    Player player = Bukkit.getPlayer(uuid);
    if (player != null && player.isOnline()) {
      return WorldGuardPlugin.inst().wrapPlayer(player);
    }

    return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
  }

  /**
   * Find a user by their playerName
   *
   * @param playerName The playerName of the user
   * @return The user
   */
  public static LocalPlayer findLocalPlayer(String playerName) {
    Player player = Bukkit.getPlayer(playerName);
    if (player != null && player.isOnline()) {
      return WorldGuardPlugin.inst().wrapPlayer(player);
    }

    return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(playerName));
  }

  /**
   * Gets the UUID of a player by their minecraftName
   *
   * @param playerName the minecraftName of the player
   * @return the UUID of the player
   */
  public static UUID getUuidByPlayerName(String playerName) {
    return Bukkit.getOfflinePlayer(playerName).getUniqueId();
  }

  /**
   * Gets the minecraftName of a player by their uuid
   *
   * @param uuid the uuid of the player
   * @return the minecraftName of the player
   */
  public static String getPlayerNameByUuid(UUID uuid) {
    return Bukkit.getOfflinePlayer(uuid).getName();
  }

}
