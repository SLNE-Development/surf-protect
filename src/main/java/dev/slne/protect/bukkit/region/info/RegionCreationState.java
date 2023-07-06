package dev.slne.protect.bukkit.region.info;

/**
 * Represents the state of a region creation
 */
public enum RegionCreationState {
	/**
	 * More markers are needed to complete the region creation
	 */
	MORE_MARKERS_NEEDED,

	/**
	 * The region is overlapping with an existing region
	 */
	OVERLAPPING,

	/**
	 * The region is too expensive
	 */
	TOO_EXPENSIVE,

	/**
	 * The region is too large
	 */
	TOO_LARGE,

	/**
	 * The region is too small
	 */
	TOO_SMALL,

	/**
	 * No currency
	 */
	NO_CURRENCY,

	/**
	 * The region creation can be completed
	 */
	SUCCESS;
}
