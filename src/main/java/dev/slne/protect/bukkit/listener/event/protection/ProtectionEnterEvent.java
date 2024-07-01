package dev.slne.protect.bukkit.listener.event.protection;

import dev.slne.protect.bukkit.listener.event.ProtectEvent;
import dev.slne.protect.bukkit.player.ProtectionPlayer;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import org.bukkit.Location;

/**
 * The type Protection enter event.
 */
public class ProtectionEnterEvent extends ProtectEvent {

  private final ProtectionPlayer enteredPlayer;
  private final ProtectionRegion enteredProtectionRegion;

  private final Location fromLocation;
  private final Location toLocation;

  /**
   * Instantiates a new Protection enter event.
   *
   * @param enteredPlayer    the entered player
   * @param protectionRegion the protection region
   * @param fromLocation     the from location
   * @param toLocation       the to location
   */
  public ProtectionEnterEvent(ProtectionPlayer enteredPlayer, ProtectionRegion protectionRegion,
      Location fromLocation, Location toLocation) {
    this.enteredPlayer = enteredPlayer;
    this.enteredProtectionRegion = protectionRegion;

    this.fromLocation = fromLocation;
    this.toLocation = toLocation;
  }

  /**
   * Gets entered player.
   *
   * @return the entered player
   */
  public ProtectionPlayer getEnteredPlayer() {
    return enteredPlayer;
  }

  /**
   * Gets entered protection region.
   *
   * @return the entered protection region
   */
  public ProtectionRegion getEnteredProtectionRegion() {
    return enteredProtectionRegion;
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
