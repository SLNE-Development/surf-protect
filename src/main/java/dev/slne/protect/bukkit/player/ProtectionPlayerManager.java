package dev.slne.protect.bukkit.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.UUID;
import org.bukkit.event.Listener;

/**
 * The type Protection player manager.
 */
public class ProtectionPlayerManager implements Listener {

  public static final ProtectionPlayerManager INSTANCE = new ProtectionPlayerManager();
  private final Object2ObjectMap<UUID, ProtectionPlayer> playerMap;

  /**
   * Instantiates a new Protection player manager.
   */
  public ProtectionPlayerManager() {
    this.playerMap = new Object2ObjectOpenHashMap<>();
  }

  /**
   * Gets protection player.
   *
   * @param player the player
   * @return the protection player
   */
  public ProtectionPlayer getProtectionPlayer(UUID player) {
    ProtectionPlayer protectionPlayer = playerMap.get(player);

    if (protectionPlayer != null) {
      return protectionPlayer;
    }

    return playerMap.put(player, new ProtectionPlayer(player));
  }

  /**
   * Add protection player.
   *
   * @param player the player
   */
  public void addProtectionPlayer(ProtectionPlayer player) {
    playerMap.put(player.getUuid(), player);
  }

  /**
   * Remove protection player.
   *
   * @param player the player
   */
  public void removeProtectionPlayer(UUID player) {
    playerMap.remove(player);
  }

  /**
   * Gets player map.
   *
   * @return the player map
   */
  public Object2ObjectMap<UUID, ProtectionPlayer> getPlayerMap() {
    return playerMap;
  }
}
