package dev.slne.protect.bukkit.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.slne.protect.bukkit.math.Mth;
import dev.slne.protect.bukkit.region.flags.ProtectionFlagsRegistry;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the protection utils
 */
public class ProtectionUtils {

    /**
     * Utility class
     */
    private ProtectionUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the {@link RegionContainer}
     *
     * @return the {@link RegionContainer}
     */
    private static RegionContainer getRegionContainer() {
        return WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    /**
     * Gets the {@link RegionManager} for the given world
     *
     * @param world the world
     * @return the {@link RegionManager}
     */
    public static RegionManager getRegionManager(World world) {
        return getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    /**
     * Gets all regions for the given {@link LocalPlayer}
     *
     * @param localPlayer the {@link LocalPlayer}
     * @return the {@link Set} of {@link Map.Entry}
     */
    public static Map<World, List<Map.Entry<String, ProtectedRegion>>> getRegionsFor(LocalPlayer localPlayer) {
        Map<World, List<Map.Entry<String, ProtectedRegion>>> regions = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);

            RegionManager manager = getRegionContainer().get(adaptedWorld);

            if (manager == null) {
                continue;
            }

            List<Map.Entry<String, ProtectedRegion>> regionMap = new ArrayList<>(
                    manager.getRegions().entrySet().stream().filter(entry -> entry.getValue().getOwners()
                            .contains(localPlayer)).toList());

            regions.put(world, regionMap);
        }

        return regions;
    }

    /**
     * Gets all regions for the given {@link LocalPlayer}
     *
     * @param localPlayer the {@link LocalPlayer}
     * @return the {@link Set} of ProtectedRegions
     */
    public static Map<World, List<ProtectedRegion>> getRegionListFor(LocalPlayer localPlayer) {
        Map<World, List<ProtectedRegion>> regions = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);

            RegionManager manager = getRegionContainer().get(adaptedWorld);

            if (manager == null) {
                continue;
            }

            List<ProtectedRegion> regionMap = new ArrayList<>(manager.getRegions().values().stream()
                    .filter(protectedRegion -> protectedRegion.getOwners()
                            .contains(localPlayer)).toList());

            regions.put(world, regionMap);
        }

        return regions;
    }

    /**
     * Tries to get a {@link RegionInfo} for a given {@link LocalPlayer} and
     * regionName
     *
     * @param localPlayer the {@link LocalPlayer}
     * @param regionName  the {@link String} regionName
     * @return the {@link RegionInfo} or <code>null</code>
     */
    public static RegionInfo getRegionInfo(LocalPlayer localPlayer, String regionName) {
        for (Map.Entry<World, List<Map.Entry<String, ProtectedRegion>>> entry : getRegionsFor(localPlayer).entrySet()) {
            for (Map.Entry<String, ProtectedRegion> region : entry.getValue()) {
                if (region.getValue().getId().equals(regionName)) {
                    return new RegionInfo(entry.getKey(), region.getValue());
                }
            }
        }

        return null;
    }

    /**
     * Checks if the given location is in the protection region
     *
     * @param location the location
     * @return true if in region
     */
    public static boolean isInProtectionRegion(Location location) {
        RegionManager manager = getRegionManager(location.getWorld());

        if (manager == null) {
            return true;
        }

        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return regions.testState(null, ProtectionFlagsRegistry.SURF_PROTECT);
    }

    /**
     * Returns if two regions overlap
     *
     * @return true if regions overlap
     */
    public static boolean doRegionsOverlap(World world, ProtectedRegion regionOne, ProtectedRegion regionTwo) {
        RegionManager manager = ProtectionUtils.getRegionManager(world);
        ApplicableRegionSet regions = manager.getApplicableRegions(regionOne);

        if (regions.size() < 1) {
            return false;
        }

        if (regionTwo == null) {
            return true;
        }

        return regions.getRegions().contains(regionTwo) ? regions.size() > 1 : regions.size() > 0;
    }

    /**
     * Checks if the given location is inside global region
     *
     * @param location the location
     * @return true if in global region
     */
    public static boolean isGlobalRegion(Location location) {
        RegionManager manager = getRegionManager(location.getWorld());

        if (manager == null) {
            return true;
        }

        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return regions.size() == 0;
    }

    /**
     * Checks if the given {@link Player} currently stands in the given
     * {@link ProtectedRegion}
     *
     * @param player          the {@link Player} to check
     * @param protectedRegion the {@link ProtectedRegion} to check
     * @return if in {@link ProtectedRegion}
     */
    public static boolean standsInProtectedRegion(Player player, ProtectedRegion protectedRegion) {
        for (ProtectedRegion region : getProtectedRegionsByLocation(player.getLocation())) {
            if (protectedRegion.equals(region)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets a {@link ProtectedRegion} by {@link com.sk89q.worldedit.util.Location}
     * without including the {@link ProtectedRegion#GLOBAL_REGION}
     *
     * @param location the {@link com.sk89q.worldedit.util.Location}
     * @return the Set of {@link ProtectedRegion} or an empty list
     */
    public static Set<ProtectedRegion> getProtectedRegionsByLocation(Location location) {
        return getProtectedRegionsByLocation(location, false);
    }

    /**
     * Returns the protected region by the given location
     *
     * @param location         the {@link com.sk89q.worldedit.util.Location}
     * @param withGlobalRegion if the global region should be included
     * @return the Set of {@link ProtectedRegion} or an empty list
     */
    public static Set<ProtectedRegion> getProtectedRegionsByLocation(Location location, boolean withGlobalRegion) {
        com.sk89q.worldedit.util.Location worldEditLocation = BukkitAdapter.adapt(location);
        RegionContainer regionContainer = getRegionContainer();
        RegionQuery regionQuery = regionContainer.createQuery();
        ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(worldEditLocation);

        if (withGlobalRegion) {
            return applicableRegionSet.getRegions();
        } else {
            return applicableRegionSet.getRegions().stream().filter(region -> !region.getId()
                    .equals(ProtectedRegion.GLOBAL_REGION)).collect(Collectors.toSet());
        }
    }

    /**
     * Returns the distance between a location and the teleport location of the
     * given {@link ProtectedRegion} if set
     *
     * @param protectedRegion the {@link ProtectedRegion}
     * @param location        the {@link Location}
     * @return the {@link Double} distance
     */
    public static double getDistanceToRegion(ProtectedRegion protectedRegion, Location location) {
        com.sk89q.worldedit.util.Location teleportFlag = protectedRegion.getFlag(Flags.TELE_LOC);

        if (teleportFlag == null) {
            return 0;
        }

        Location teleportLocation = BukkitAdapter.adapt(teleportFlag);

        if (location == null) {
            return 0;
        }

        return location.distance(teleportLocation);
    }

    /**
     * Gets the area for the given region
     *
     * @param region the region
     * @return the area
     */
    public static long getArea(ProtectedRegion region) {
        Polygonal2DRegion worldEditRegion = new Polygonal2DRegion(null, region.getPoints(),
                region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());

        worldEditRegion.setMaximumY(0);
        worldEditRegion.setMinimumY(0);

        return worldEditRegion.getVolume();
    }

    /**
     * Returns the price per block for the given location
     *
     * @param protectionLocation the location
     * @return the price per block
     */
    public static double getProtectionPricePerBlock(Location protectionLocation) {
        Location protectionLocationClone = protectionLocation.clone();
        Location spawnLocation = protectionLocation.getWorld().getSpawnLocation().clone();

        spawnLocation.setY(0);
        protectionLocationClone.setY(0);

        double distance = protectionLocationClone.distance(spawnLocation);
        double spawnProtection = ProtectionSettings.PRICE_PER_BLOCK_SPAWN_PROTECTION;

        if (distance < spawnProtection) {
            return Long.MAX_VALUE;
        }

        return Mth.calculatePricePerBlock(distance);
    }

    /**
     * Returns the member names of a region
     *
     * @param region the region
     * @return the member names
     */
    public static List<String> getMemberNames(ProtectedRegion region) {
        List<String> memberNames = new ArrayList<>();

        for (UUID memberUuid : region.getMembers().getPlayerDomain().getUniqueIds()) {
            memberNames.add(ProtectionUserFinder.getPlayerNameByUuid(memberUuid));
        }

        return memberNames;
    }

    /**
     * Returns the owner names of a region
     *
     * @param region the region
     * @return the owner names
     */
    public static List<String> getOwnerNames(ProtectedRegion region) {
        List<String> ownersNames = new ArrayList<>();

        for (UUID ownerUuid : region.getOwners().getPlayerDomain().getUniqueIds()) {
            ownersNames.add(ProtectionUserFinder.getPlayerNameByUuid(ownerUuid));
        }

        return ownersNames;
    }
}
