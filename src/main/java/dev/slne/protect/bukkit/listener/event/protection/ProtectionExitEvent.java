package dev.slne.protect.bukkit.listener.event.protection;

import dev.slne.protect.bukkit.listener.event.ProtectEvent;
import dev.slne.protect.bukkit.player.ProtectionPlayer;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import org.bukkit.Location;

/**
 * The type Protection exit event.
 */
public class ProtectionExitEvent extends ProtectEvent {

  private final ProtectionPlayer exitedPlayer;
  private final ProtectionRegion exitedProtectionRegion;

  private final Location fromLocation;
  private final Location toLocation;

  /**
   * Instantiates a new Protection exit event.
   *
   * @param exitedPlayer     the exited player
   * @param protectionRegion the protection region
   * @param fromLocation     the from location
   * @param toLocation       the to location
   */
  public ProtectionExitEvent(ProtectionPlayer exitedPlayer, ProtectionRegion protectionRegion,
      Location fromLocation, Location toLocation) {
    this.exitedPlayer = exitedPlayer;
    this.exitedProtectionRegion = protectionRegion;

    this.fromLocation = fromLocation;
    this.toLocation = toLocation;
  }

  /**
   * Gets exited player.
   *
   * @return the exited player
   */
  public ProtectionPlayer getExitedPlayer() {
    return exitedPlayer;
  }

  /**
   * Gets exited protection region.
   *
   * @return the exited protection region
   */
  public ProtectionRegion getExitedProtectionRegion() {
    return exitedProtectionRegion;
  }

  /**
   * Gets from location.
   *
   * @return the from location
   */
  public Location getFromLocation() {
    return fromLocation;
  }

  /**
   * Gets to location.
   *
   * @return the to location
   */
  public Location getToLocation() {
    return toLocation;
  }
}
