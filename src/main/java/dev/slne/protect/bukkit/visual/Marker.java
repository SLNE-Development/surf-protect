package dev.slne.protect.bukkit.visual;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import dev.slne.protect.bukkit.regions.RegionCreation;

public class Marker {

	private final RegionCreation regionCreation;
	private final Location location;
	private final BlockData previousData;

	/**
	 * Creates a marker for user input markers
	 *
	 * @param regionCreation the region creation
	 * @param location       the location
	 * @param previousData   the previous data
	 */
	public Marker(RegionCreation regionCreation, Location location, BlockData previousData) {
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
	public Marker(RegionCreation regionCreation, Location location) {
		this(regionCreation, location, null);
	}

	public BlockData getPreviousData() {
		return previousData;
	}

	public Location getLocation() {
		return location;
	}

	public RegionCreation getRegionCreation() {
		return regionCreation;
	}

	public boolean hasPreviousData() {
		return previousData != null;
	}

	public int getX() {
		return location.getBlockX();
	}

	public int getZ() {
		return location.getBlockZ();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Marker marker = (Marker) o;
		return Objects.equals(regionCreation, marker.regionCreation) && Objects.equals(location, marker.location)
				&& marker.hasPreviousData() == hasPreviousData();
	}

	@Override
	public int hashCode() {
		return Objects.hash(regionCreation, location, hasPreviousData());
	}
}
