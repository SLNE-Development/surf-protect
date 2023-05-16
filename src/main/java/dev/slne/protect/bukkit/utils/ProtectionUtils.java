package dev.slne.protect.bukkit.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;

import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ProtectionUtils {

	private static final WorldGuard worldGuard = WorldGuard.getInstance();
	private static final RegionContainer container = worldGuard.getPlatform().getRegionContainer();

	/**
	 * Gets the {@link RegionManager} for the given world
	 *
	 * @param world the world
	 * @return the {@link RegionManager}
	 */
	public static RegionManager getRegionManager(World world) {
		RegionManager manager = container.get(BukkitAdapter.adapt(world));
		return manager;
	}

	public static Component getRegionUsersComponent(List<LocalPlayer> regionUsers) {
		TextComponent.Builder memberComponentBuilder = Component.text();
		Iterator<LocalPlayer> memberIterator = regionUsers.iterator();

		memberComponentBuilder.append(Component.text("[", MessageManager.SPACER));

		ProfileCache cache = WorldGuard.getInstance().getProfileCache();
		while (memberIterator.hasNext()) {
			LocalPlayer memberUser = memberIterator.next();
			String userName = memberUser.getName();
			if (userName == null) {
				Profile profile = cache.getIfPresent(memberUser.getUniqueId());

				if (profile != null) {
					userName = profile.getName();
				}
			}

			memberComponentBuilder.append(Component.text(userName, MessageManager.VARIABLE_VALUE));

			if (memberIterator.hasNext()) {
				memberComponentBuilder.append(Component.text(", ", MessageManager.SPACER));
			}

		}

		memberComponentBuilder.append(Component.text("]", MessageManager.SPACER));

		return memberComponentBuilder.build();
	}

	public static Component getRegionOwnersMembersComponent(RegionInfo regionInfo) {
		TextComponent.Builder builder = Component.text();

		List<LocalPlayer> regionOwners = regionInfo.getOwners();
		List<LocalPlayer> regionMembers = regionInfo.getMembers();

		if (regionOwners != null) {
			builder.append(Component.text("Besitzer: ", MessageManager.VARIABLE_KEY));
			builder.append(getRegionUsersComponent(regionOwners));
		}

		if (regionOwners != null && regionMembers != null) {
			builder.append(Component.text(", ", MessageManager.SPACER));
		}

		if (regionMembers != null) {
			builder.append(Component.text("Mitglieder: ", MessageManager.VARIABLE_KEY));
			builder.append(getRegionUsersComponent(regionMembers));
		}

		return builder.build();
	}

	public static Set<Map.Entry<String, ProtectedRegion>> getRegionsFor(LocalPlayer lp) {
		Set<Map.Entry<String, ProtectedRegion>> regions = new HashSet<>();

		for (World world : Bukkit.getWorlds()) {
			com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(world);

			RegionManager manager = container.get(adaptedWorld);

			if (manager == null) {
				return new HashSet<Map.Entry<String, ProtectedRegion>>();
			}

			regions.addAll(manager.getRegions().entrySet().stream().filter((entry) -> {
				return entry.getValue().getOwners().contains(lp);
			}).collect(Collectors.toSet()));
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
		return getRegionsFor(localPlayer).stream().map(regionPredicate -> new RegionInfo(regionPredicate.getValue()))
				.filter(regionInfo -> {
					return regionInfo != null && regionInfo.getName().equals(regionName);
				}).findFirst().orElse(null);
	}

	/**
	 * Checks if the given location is in the protection region
	 *
	 * @param location the location
	 * @return true if in region
	 */
	public static boolean isInProtectionRegion(Location location) {
		RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));

		if (manager == null) {
			return true;
		}

		ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
		return regions.testState(null, ProtectionSettings.SURVIVAL_PROTECT);
	}

	/**
	 * Checks if the given location is inside global region
	 *
	 * @param location the location
	 * @return true if in global region
	 */
	public static boolean isGlobalRegion(Location location) {
		RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));

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
		ProtectedRegion currentRegion = getProtectedRegionByLocation(player.getLocation());
		return currentRegion != null && protectedRegion.equals(currentRegion);
	}

	/**
	 * Gets a {@link ProtectedRegion} by {@link com.sk89q.worldedit.util.Location}
	 * without including the {@link ProtectedRegion#GLOBAL_REGION}
	 * 
	 * @param location the {@link com.sk89q.worldedit.util.Location}
	 * @return the {@link ProtectedRegion} or <code>null</code>
	 */
	public static ProtectedRegion getProtectedRegionByLocation(Location location) {
		com.sk89q.worldedit.util.Location worldEditLocation = BukkitAdapter.adapt(location);
		RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionQuery regionQuery = regionContainer.createQuery();

		ApplicableRegionSet applicableRegionSet = regionQuery.getApplicableRegions(worldEditLocation);
		return applicableRegionSet.getRegions().stream().filter(region -> {
			return !region.getId().equals(ProtectedRegion.GLOBAL_REGION);
		}).findFirst().orElse(null);
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
		Location teleportLocation = BukkitAdapter.adapt(protectedRegion.getFlag(Flags.TELE_LOC));

		if (teleportLocation == null || location == null) {
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
		Polygonal2DRegion weRegion = new Polygonal2DRegion(null, region.getPoints(),
				region.getMinimumPoint().getBlockY(), region.getMaximumPoint().getBlockY());
		weRegion.setMaximumY(0);
		weRegion.setMinimumY(0);
		return weRegion.getVolume();
	}
}
