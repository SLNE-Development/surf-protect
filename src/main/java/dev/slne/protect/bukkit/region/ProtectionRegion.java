package dev.slne.protect.bukkit.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import java.util.List;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * The type Protection region.
 */
public class ProtectionRegion {

  private static final ComponentLogger LOGGER = ComponentLogger.logger(ProtectionRegion.class);

  private ProtectedRegion protectedRegion;

  /**
   * Gets by location.
   *
   * @param location the location
   * @return the by location
   */
  public static List<ProtectionRegion> getByLocation(Location location) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Gets by protected region.
   *
   * @param protectedRegion the protected region
   * @return the by protected region
   */
  public static List<ProtectionRegion> getByProtectedRegion(ProtectedRegion protectedRegion) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Gets world guard region.
   *
   * @return the world guard region
   */
  public ProtectedRegion getWorldGuardRegion() {
    return protectedRegion;
  }

  /**
   * Gets center vector.
   *
   * @return the center vector
   */
  public Vector3 getCenterVector3() {
    if (protectedRegion == null) {
      LOGGER.error("Protected region is null and trying to access center location");

      throw new IllegalStateException("Protected region is null");
    }

    return WorldEditRegionConverter.convertToRegion(protectedRegion).getCenter();
  }

  /**
   * Gets center location.
   *
   * @param world the world
   * @return the center location
   */
  public Location getCenterLocation(World world) {
    Vector3 center = getCenterVector3();

    return new Location(world, center.x(), center.y(), center.z());
  }

  /**
   * Gets teleport location.
   *
   * @param world the world
   * @return the teleport location
   */
  public Location getTeleportLocation(World world) {
    if (protectedRegion == null) {
      LOGGER.error("Protected region is null and trying to access teleport location");

      throw new IllegalStateException("Protected region is null");
    }

    com.sk89q.worldedit.util.Location flagLocation = protectedRegion.getFlag(Flags.TELE_LOC);

    if (flagLocation == null) {
      return getCenterLocation(world);
    }

    return BukkitAdapter.adapt(flagLocation);
  }

  /**
   * Sets teleport location.
   *
   * @param world the world
   */
  public void setTeleportLocationToCenter(World world) {
    if (protectedRegion == null) {
      LOGGER.error("Protected region is null and trying to set teleport location");

      throw new IllegalStateException("Protected region is null");
    }

    Location centerLocation = getCenterLocation(world);
    centerLocation.setY(
        world.getHighestBlockYAt(centerLocation.getBlockX(), centerLocation.getBlockZ()));
    centerLocation.add(0.5, 5, 0.5);
    centerLocation.setPitch(90);
    centerLocation.setYaw(0);

    protectedRegion.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(centerLocation));
  }

}
