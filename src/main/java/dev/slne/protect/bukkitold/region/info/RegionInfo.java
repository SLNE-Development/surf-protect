package dev.slne.protect.bukkitold.region.info;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.region.flag.info.ProtectionFlagInfo;
import dev.slne.protect.bukkitold.math.Mth;
import dev.slne.protect.bukkitold.region.ProtectionUtils;
import dev.slne.protect.bukkitold.user.ProtectionUserFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents the info of a region
 */
public class RegionInfo {

  private final ProtectedRegion region;
  private final World world;
  private ProtectionFlagInfo info;
  private List<LocalPlayer> owners;
  private List<LocalPlayer> members;

  /**
   * Construct a new region info
   * <p>
   * Also fetches the surf protect flag from the region
   *
   * @param region The region
   */
  public RegionInfo(World world, ProtectedRegion region) {
    this.region = region;
    this.info = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG);
    this.world = world;

    if (this.info == null) {
      this.info = setProtectionInfoToRegion(new ProtectionFlagInfo(this.region.getId()));
    }

    fetchAllPlayers();
  }

  /**
   * Fetches all players from the region
   */
  public void fetchAllPlayers() {
    this.owners = fetchPlayers(region.getOwners());
    this.members = fetchPlayers(region.getMembers());
  }

  /**
   * Gets the area of the region
   *
   * @return The area
   */
  public long getArea() {
    return ProtectionUtils.getArea(this.region);
  }

  /**
   * Gets the price of the region
   *
   * @return The price
   */
  public double getPrice() {
    return Mth.getRegionPrice(this.region);
  }

  /**
   * Gets the smallest distance from the spawn
   *
   * @param points The points
   * @return The smallest distance
   */
  private BlockVector2 getSmallestDistance(
      List<BlockVector2> points) { // TODO: 04.02.2024 18:01 - remove?
    BlockVector2 smallestDistance = null;
    BlockVector2 spawnPoint = BukkitAdapter.asBlockVector(world.getSpawnLocation())
        .toBlockVector2();

    for (BlockVector2 point : points) {
      if (smallestDistance == null) {
        smallestDistance = point;
        continue;
      }

      if (point.distanceSq(spawnPoint) < smallestDistance.distanceSq(spawnPoint)) {
        smallestDistance = point;
      }
    }

    return smallestDistance;
  }

  /**
   * Gets the retail price
   *
   * @return The retail price
   */
  public double getRetailPrice() {
    return Mth.getRegionRetailPrice(this.region);
  }

  /**
   * Gets the name of the region
   * <p>
   * If the protection flag info is set the name of the flag is used, otherwise its the id of the
   * region
   *
   * @return The name
   */
  public String getName() {
    return this.info != null ? this.info.name() : this.region.getId();
  }

  /**
   * Gets the protection flag info
   *
   * @return The info
   */
  public ProtectionFlagInfo getProtectionFlagInfo() {
    return info;
  }

  /**
   * Sets the protection flag info to the region
   *
   * @param info The info
   * @return The protection flag info
   */
  public ProtectionFlagInfo setProtectionInfoToRegion(ProtectionFlagInfo info) {
    this.info = info;
    this.region.setFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG, this.info);

    return this.info;
  }

  /**
   * Gets the teleport location
   *
   * @return The teleport location
   */
  public @Nullable Location getTeleportLocation() {
    com.sk89q.worldedit.util.Location teleportState = region.getFlag(Flags.TELE_LOC);

    if (teleportState == null) {
      return null;
    }

    return BukkitAdapter.adapt(teleportState);
  }

  /**
   * Fetches all players from the given domain
   *
   * @param domain The domain
   * @return The players
   */
  public List<LocalPlayer> fetchPlayers(DefaultDomain domain) {
    List<LocalPlayer> localPlayers = new ArrayList<>();
    Set<UUID> uuids = domain.getUniqueIds();

    for (UUID uuid : uuids) {
      LocalPlayer localPlayer = ProtectionUserFinder.findLocalPlayer(uuid);

      if (localPlayer != null) {
        localPlayers.add(localPlayer);
      }
    }

    return localPlayers;
  }

  /**
   * Gets the region
   *
   * @return The region
   */
  public ProtectedRegion getRegion() {
    return region;
  }

  /**
   * Gets the members of this region
   *
   * @return The members
   */
  public List<LocalPlayer> getMembers() {
    return members;
  }

  /**
   * Gets the owners of this region
   *
   * @return The owners
   */
  public List<LocalPlayer> getOwners() {
    return owners;
  }

  /**
   * @return the world
   */
  public World getWorld() {
    return world;
  }

}
