package dev.slne.surf.protect.paper.region.settings

import org.bukkit.HeightMap

/**
 * Represents the protection settings
 */
object ProtectionSettings {
    /**
     * How many markers should the user get when creating a region
     */
    const val MARKERS: Int = 8

    const val MARKES_WRITTEN: String = "acht"

    /**
     * The minimum amount of markers needed to create a region
     */
    const val MIN_MARKERS: Int = 4

    /**
     * The minimum amount of markers to connect the last marker to the first marker
     */
    const val MIN_MARKERS_LAST_CONNECTION: Int = 3

    /**
     * The Cooldown between region creations
     */
    const val REGION_CREATION_COOLDOWN: Int = 30

    /**
     * The marker key used to identify markers
     */
    const val MARKER_KEY: String = "SAFIUZdfx"

    /**
     * The maximum amount of blocks a region can have
     */
    val AREA_MAX_BLOCKS: Int = Int.Companion.MAX_VALUE

    /**
     * The minimum amount of blocks a region can have
     */
    const val AREA_MIN_BLOCKS: Int = 250

    /**
     * The minimum y coordinate of a region
     */
    val MIN_Y_WORLD: Int = -64

    /**
     * The maximum y coordinate of a region
     */
    const val MAX_Y_WORLD: Int = 319

    /**
     * The maximum distance a user can go from the to be expanded region
     */
    const val MAX_EXPAND_DISTANCE: Double = 20.0

    /**
     * The money the user is getting per block when selling
     */
    const val RETAIL_MODIFIER: Double = 0.65

    /**
     * The height map used for protection
     */
    val PROTECTION_HEIGHTMAP: HeightMap = HeightMap.MOTION_BLOCKING_NO_LEAVES

    /**
     * The max distance a user is allowed to go from the protection start before being thrown back
     */
    const val MAX_DISTANCE_FROM_PROTECTION_START: Int = 100

    /**
     * The max distance a user is allowed to go from the protection start before being thrown back
     */
    val MAX_DISTANCE_FROM_PROTECTION_START_SQUARED: Int = 100 * 100

    /**
     * Price to rename a protection
     */
    const val PROTECTION_RENAME_PRICE: Int = 2500

    /**
     * The minimum amount of blocks squared a player can be away from the protection visualization
     */
    @JvmField
    val PROTECTION_VISUALIZER_MIN_DISTANCE: Int = 30 * 30

    /**
     * The maximum amount of blocks squared a player can be away from the protection visualization
     */
    @JvmField
    val PROTECTION_VISUALIZER_MAX_DISTANCE: Int = 200 * 200

    /**
     * The height of the protection visualizer
     */
    const val PROTECTION_VISUALIZER_HEIGHT: Int = 5

    /**
     * The time in seconds between protection visualizer updates
     */
    const val PROTECTION_VISUALIZER_UPDATE_INTERVAL: Int = 5

    /**
     * The price per block breakpoint
     */
    const val PRICE_PER_BLOCK: Double = 4.0

    /**
     * The price per block breakpoint
     */
    const val PRICE_PER_BLOCK_SPAWN_PROTECTION: Double = 200.0

    const val CURRENCY_NAME: String = "CastCoin"

    const val RANDOM_NAME_LENGTH: Int = 5
}
