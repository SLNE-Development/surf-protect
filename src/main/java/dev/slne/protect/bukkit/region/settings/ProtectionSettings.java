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
    public static final int MIN_MARKERS = 4;

    /**
     * The minimum amount of markers to connect the last marker to the first marker
     */
    public static final int MIN_MARKERS_LAST_CONNECTION = 3;

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
     * The money the user is getting per block when selling
     */
    public static final double RETAIL_MODIFIER = 0.65f;

    /**
     * The height map used for protection
     */
    public static final HeightMap PROTECTION_HEIGHTMAP = HeightMap.MOTION_BLOCKING_NO_LEAVES;

    /**
     * The max distance a user is allowed to go from the protection start before
     * being thrown back
     */
    public static final int MAX_DISTANCE_FROM_PROTECTION_START = 150;

    /**
     * The max distance a user is allowed to go from the protection start before
     * being thrown back
     */
    public static final int MAX_DISTANCE_FROM_PROTECTION_START_SQUARED = 150 * 150;

    /**
     * Price to rename a protection
     */
    public static final int PROTECTION_RENAME_PRICE = 2500;

    /**
     * The minimum amount of blocks squared a player can be away from the protection
     * visualization
     */
    public static final int PROTECTION_VISUALIZER_MIN_DISTANCE = 30 * 30;

    /**
     * The maximum amount of blocks squared a player can be away from the protection
     * visualization
     */
    public static final int PROTECTION_VISUALIZER_MAX_DISTANCE = 200 * 200;

    /**
     * The height of the protection visualizer
     */
    public static final int PROTECTION_VISUALIZER_HEIGHT = 5;

    /**
     * The time in seconds between protection visualizer updates
     */
    public static final int PROTECTION_VISUALIZER_UPDATE_INTERVAL = 5;

    /**
     * The price per block breakpoint
     */
    public static final double PRICE_PER_BLOCK = 4;

    /**
     * The price per block breakpoint
     */
    public static final double PRICE_PER_BLOCK_SPAWN_PROTECTION = 200;

    /**
     * A utility class
     */
    private ProtectionSettings() {
        throw new IllegalStateException("Utility class");
    }

}
