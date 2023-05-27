package dev.slne.protect.bukkit.region.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;

/**
 * Represents the info of a region
 */
public class RegionInfo {

	private ProtectionFlagInfo info;
	private final ProtectedRegion region;

	private List<LocalPlayer> owners;
	private List<LocalPlayer> members;

	/**
	 * Construct a new region info
	 *
	 * Also fetches the surf protect flag from the region
	 *
	 * @param region The region
	 */
	public RegionInfo(ProtectedRegion region) {
		this.region = region;
		this.info = region.getFlag(ProtectionFlags.SURF_PROTECT_FLAG);

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
	public float getPrice() {
		return getArea() * ProtectionSettings.PRICE_PER_BLOCK;
	}

	/**
	 * Gets the retail price
	 *
	 * @return The retail price
	 */
	public float getRetailPrice() {
		return getPrice() * ProtectionSettings.RETAIL_MODIFIER;
	}

	/**
	 * Gets the name of the region
	 *
	 * If the protection flag info is set the name of the flag is used, otherwise
	 * its the id of the region
	 *
	 * @return The name
	 */
	public String getName() {
		return this.info != null ? this.info.getName() : this.region.getId();
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
		this.region.setFlag(ProtectionFlags.SURF_PROTECT_FLAG, this.info);

		return this.info;
	}

	/**
	 * Gets the teleport location
	 *
	 * @return The teleport location
	 */
	public Location getTeleportLocation() {
		return BukkitAdapter.adapt(region.getFlag(Flags.TELE_LOC));
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

}
