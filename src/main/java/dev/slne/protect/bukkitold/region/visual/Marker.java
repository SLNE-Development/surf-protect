package dev.slne.protect.bukkitold.region.visual;

import dev.slne.protect.bukkitold.region.ProtectionRegion;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class Marker {

  private final ProtectionRegion regionCreation;
  private final Location location;
  private final BlockData previousData;

  /**
   * Creates a marker for user input markers
   *
   * @param regionCreation the region creation
   * @param location       the location
   * @param previousData   the previous data
   */
  public Marker(ProtectionRegion regionCreation, Location location, BlockData previousData) {
    this.regionCreation = regionCreation;
    this.location = location;
    this.previousData = previousData;
  }

  /**
   * Creates non-world-changing marker
   *
   * @param regionCreation the region creation
   * @param location       the location
   */
  public Marker(ProtectionRegion regionCreation, Location location) {
    this(regionCreation, location, null);
  }

  /**
   * Restores the previous data
   */
  public void restorePreviousData() {
    if (hasPreviousData()) {
      location.getBlock().setBlockData(previousData, true);
    }
  }

  /**
   * Checks if the marker has previous data
   *
   * @return true if the marker has previous data
   */
  public boolean hasPreviousData() {
    return previousData != null;
  }

  /**
   * Gets the region creation
   *
   * @return the region creation
   */
  public BlockData getPreviousData() {
    return previousData;
  }

  /**
   * Gets the location
   *
   * @return the location
   */
  public Location getLocation() {
    return location;
  }

  /**
   * Gets the region creation
   *
   * @return the region creation
   */
  public ProtectionRegion getRegionCreation() {
    return regionCreation;
  }

  /**
   * Gets the block X coordinate
   *
   * @return the block X coordinate
   */
  public int getBlockX() {
    return location.getBlockX();
  }

  /**
   * Gets the block Y coordinate
   *
   * @return the block Y coordinate
   */
  public int getBlockY() {
    return location.getBlockY();
  }

  /**
   * Gets the block Z coordinate
   *
   * @return the block Z coordinate
   */
  public int getBlockZ() {
    return location.getBlockZ();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }

    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    Marker marker = (Marker) other;

    return Objects.equals(regionCreation, marker.regionCreation) && Objects.equals(location,
        marker.location)
        && marker.hasPreviousData() == hasPreviousData();
  }

  @Override
  public int hashCode() {
    return Objects.hash(regionCreation, location, hasPreviousData());
  }
}
