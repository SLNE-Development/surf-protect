package dev.slne.protect.bukkitold.region;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

/**
 * Represents a temporary protection region
 * <p>
 * A temporary protection region is a region that is not yet protected
 */
public class TemporaryProtectionRegion {

  private final ProtectedRegion region;
  private final RegionManager manager;
  private final World world;

  private long effectiveArea;

  /**
   * Construct a new temporary protection region
   *
   * @param world   the world
   * @param region  the region
   * @param manager the region manager
   */
  public TemporaryProtectionRegion(World world, ProtectedRegion region, RegionManager manager) {
    this.world = world;
    this.region = region;
    this.manager = manager;
  }

  /**
   * Gets the actual protected region
   *
   * @return the protected region
   */
  public ProtectedRegion getRegion() {
    return region;
  }

  /**
   * Gets the region manager
   *
   * @return the region manager
   */
  public RegionManager getManager() {
    return manager;
  }

  /**
   * Gets the area
   *
   * @return the area of this region
   */
  public long getArea() {
    return ProtectionUtils.getArea(this.region);
  }

  /**
   * Gets the effective area
   *
   * @return the effective area
   */
  public long getEffectiveArea() {
    return effectiveArea;
  }

  /**
   * Sets the effective area
   *
   * @param effectiveArea the new effective area
   */
  public void setEffectiveArea(long effectiveArea) {
    this.effectiveArea = effectiveArea;
  }

  /**
   * Checks if this region overlaps with a unowned region for this player
   *
   * @param localPlayer the player
   * @return whether the local player has a region he is owning
   */
  public boolean overlapsUnownedRegion(LocalPlayer localPlayer) {
    return manager.overlapsUnownedRegion(this.region, localPlayer);
  }

  /**
   * Checks if this region overlaps with the given region
   *
   * @param other the other region
   * @return whether the given region overlaps with this one
   */
  public boolean overlaps(ProtectedRegion other) {
    return ProtectionUtils.doRegionsOverlap(this.world, this.region, other);
  }

  /**
   * Protects the given region
   */
  public void protect() {
    manager.addRegion(region);
  }

  /**
   * Gets the world
   *
   * @return the world
   */
  public World getWorld() {
    return world;
  }
}
