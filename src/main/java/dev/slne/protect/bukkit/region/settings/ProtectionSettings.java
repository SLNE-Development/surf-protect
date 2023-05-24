package dev.slne.protect.bukkit.region.settings;

import org.bukkit.HeightMap;

/**
 * Represents the protection settings
 */
public class ProtectionSettings {

	/**
	 * How many markers should the user get when creating a region
	 */
	public static final int MARKERS = 8;

	/**
	 * The minimum amount of markers needed to create a region
	 */
	public static final int MIN_MARKERS = 5;

	/**
	 * The Cooldown between region creations
	 */
	public static final int REGION_CREATION_COOLDOWN = 30;

	/**
	 * The marker key used to identify markers
	 */
	public static final String MARKER_KEY = "SAFIUZdfx";

	/**
	 * The maximum amount of blocks a region can have
	 */
	public static final int AREA_MAX_BLOCKS = Integer.MAX_VALUE;

	/**
	 * The minimum amount of blocks a region can have
	 */
	public static final int AREA_MIN_BLOCKS = 250;

	/**
	 * The minimum y coordinate of a region
	 */
	public static final int MIN_Y_WORLD = -64;

	/**
	 * The maximum y coordinate of a region
	 */
	public static final int MAX_Y_WORLD = 319;

	/**
	 * The maximum distance a user can go from the to be expanded region
	 */
	public static final double MAX_EXPAND_DISTANCE = 20;

	/**
	 * The price the user is paying per block
	 */
	public static final float PRICE_PER_BLOCK = 5f;

	/**
	 * The money the user is getting per block when selling
	 */
	public static final float RETAIL_MODIFIER = 0.5f;

	/**
	 * The height map used for protection
	 */
	public static final HeightMap PROTECTION_HEIGHTMAP = HeightMap.MOTION_BLOCKING_NO_LEAVES;

	/**
	 * The max distance a user is allowed to go from the protection start before
	 * being thrown back
	 */
	public static final int MAX_DISTANCE_FROM_PROTECTION_START = 100 * 100;

	/**
	 * The max distance a user is allowed to go from the protection start before
	 * being teleported back
	 */
	public static final int MAX_DISTANCE_FROM_PROTECTION_START_TELEPORT = MAX_DISTANCE_FROM_PROTECTION_START
			+ (20 * 20);

	/**
	 * The force a user is thrown back with when too far away from the protection
	 * start
	 */
	public static final double MAX_DISTANCE_FROM_PROTECTION_START_FORCE = .2d;

	/**
	 * Price to rename a protection
	 */
	public static final int PROTECTION_RENAME_PRICE = 2500;

	/**
	 * A utility class
	 */
	private ProtectionSettings() {
		throw new IllegalStateException("Utility class");
	}

}
