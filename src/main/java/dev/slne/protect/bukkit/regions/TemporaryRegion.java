package dev.slne.protect.bukkit.regions;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.utils.ProtectionUtils;

public class TemporaryRegion {
	private final ProtectedRegion region;
	private final RegionManager manager;
	private long effectiveArea;

	public TemporaryRegion(ProtectedRegion region, RegionManager manager) {
		this.region = region;
		this.manager = manager;
	}

	public ProtectedRegion getRegion() {
		return region;
	}

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
		ApplicableRegionSet regions = manager.getApplicableRegions(this.region);
		if (regions.size() < 1) {
			return false;
		}
		if (other == null) {
			return true;
		}
		return regions.getRegions().contains(other) ? regions.size() > 1 : regions.size() > 0;
	}

	/**
	 * Protects the given region
	 */
	public void protect() {
		manager.addRegion(region);
	}
}
