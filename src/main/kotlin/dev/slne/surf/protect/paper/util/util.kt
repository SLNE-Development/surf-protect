@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.util

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import io.papermc.paper.math.Position
import org.bukkit.Bukkit
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs
import com.sk89q.worldedit.util.Location as WorldEditLocation
import com.sk89q.worldedit.world.World as WorldEditWorld

/**
 * Calculates the approximate center point of a protected region.
 *
 * This method computes the center by adding the minimum and maximum points
 * of the region and dividing the result by two. The calculation operates
 * on the coordinates of the region's boundaries and returns the center point
 * as a `BlockVector3` object.
 *
 * @return the center point of the region as a `BlockVector3`
 */
fun ProtectedRegion.fastCenter(): BlockVector3 = minimumPoint.add(maximumPoint).divide(2)

/**
 * Converts a 2D block vector to a 3D block position by adding an optional Y-coordinate.
 *
 * @param y The Y-coordinate to be used for the resulting block position. Defaults to 0 if not specified.
 */
fun BlockVector2.toBlockPosition(y: Int = 0) = Position.block(x(), y, z())

/**
 * Attempts to convert a UUID into a local player instance. If the player corresponding
 * to the UUID is currently online, it wraps the online player; otherwise, it wraps the
 * offline player associated with the UUID.
 *
 * @return A LocalPlayer instance corresponding to the UUID.
 */
fun UUID.toLocalPlayer(): LocalPlayer {
    val player = Bukkit.getPlayer(this)
    return if (player != null && player.isOnline) {
        WorldGuardPlugin.inst().wrapPlayer(player)
    } else {
        WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(this))
    }
}

/**
 * Provides access to WorldGuard's region container for handling region-related operations.
 *
 * This property offers a global entry point for accessing the `RegionContainer` instance, which is crucial for
 * interacting with regions defined within WorldGuard. It enables querying, loading, and manipulating protected regions
 * across different WorldGuard-managed worlds.
 *
 * The `regionContainer` is primarily used to retrieve various region-related data through its API, including:
 * - Fetching region managers for specific worlds.
 * - Interacting with loaded regions or querying region-related attributes.
 * - Constructing queries to determine applicable regions for a given context (e.g., locations or players).
 *
 * This property retrieves the `RegionContainer` directly from the WorldGuard instance, ensuring that the latest and
 * correct instance is available for use by other functions and data classes.
 */
val regionContainer: RegionContainer get() = WorldGuard.getInstance().platform.regionContainer

/**
 * Retrieves the region manager for the calling `World` instance, returning `null` if unavailable.
 *
 * The region manager is a component provided by WorldEdit for managing protection regions
 * within a specific world. This method adapts the `World` instance to the WorldEdit
 * equivalent and queries the associated region container.
 *
 * @receiver The `World` instance for which to retrieve the region manager.
 * @return The region manager associated with the world, or `null` if no region manager is available.
 */
fun World.getRegionManagerOrNull() = regionContainer.get(toWorldEdit())

/**
 * Retrieves the region manager for the current `World` instance.
 *
 * This method attempts to fetch the `RegionManager` associated with the given `World`. If no
 * region manager is available, an exception is thrown indicating the absence of a region manager
 * for the specified world.
 *
 * The region manager is primarily used for handling protection regions within a WorldEdit-integrated context.
 *
 * @receiver The `World` instance for which to retrieve the region manager.
 * @return The region manager associated with the world.
 * @throws IllegalStateException If the region manager is not found for the world.
 */
fun World.getRegionManager() =
    getRegionManagerOrNull() ?: error("Region manager not found for world: ${this.name}")

/**
 * Adapts a Bukkit `World` instance to its equivalent WorldEdit `World` representation.
 *
 * This method is commonly used when integrating Bukkit-based systems with WorldEdit,
 * allowing for operations on the WorldEdit API using the given `World`.
 *
 * @receiver The Bukkit `World` instance to adapt.
 * @return The adapted WorldEdit `World` representation of the Bukkit `World`.
 */
fun World.toWorldEdit(): WorldEditWorld = BukkitAdapter.adapt(this)

/**
 * Retrieves the RegionManager associated with the current ProtectedRegion, if available.
 * This method searches for a RegionManager that contains the region with the specified ID.
 *
 * @return The associated RegionManager, or null if no such RegionManager exists.
 */
fun ProtectedRegion.getRegionManagerOrNull(): RegionManager? =
    regionContainer.loaded.find { it.hasRegion(id) }

/**
 * Calculates the volume of a protected region, which can either be flat (2D area) or include height (3D volume).
 *
 * @param flat Indicates whether to calculate a 2D flat area (true) or the 3D volume (false). Defaults to true.
 * @return The calculated volume as a [Long].
 */
// copy the volume calculation from WorldEdit, since the worldguard developer is incapable of doing it --> https://github.com/EngineHub/WorldGuard/pull/1930
fun ProtectedRegion.fixedVolume(flat: Boolean = true): Long {
    val n = points.size
    if (n < 3) return 0L

    var twiceArea = 0L      // 2 × actual area (can be negative)
    var j = n - 1
    for (i in 0 until n) {
        val pi = points[i]
        val pj = points[j]
        twiceArea += (pj.x() + pi.x()).toLong() * (pj.z() - pi.z())
        j = i
    }

    val baseArea = abs(twiceArea) / 2L
    return if (flat) baseArea else {
        val height = (maximumPoint.y() - minimumPoint.y() + 1).toLong()
        baseArea * height
    }
}

/**
 * Retrieves a list of all regions owned by the local player.
 *
 * This method filters the loaded regions from the region container to identify those where
 * the local player is listed as an owner. It processes the regions into a sequence, extracts
 * their values, and subsequently transforms them into a list of objects.
 *
 * @receiver The local player for whom to retrieve the regions.
 * @return A list of regions owned by the local player.
 */
fun LocalPlayer.allRegions() = regionContainer.loaded.asSequence()
    .flatMap {
        it.regions.asSequence()
            .filter { (_, region) -> region.owners.contains(this) }
    }
    .map { it.value }
    .toObjectList()

/**
 * Checks whether the location is within a defined protection region.
 *
 * This method retrieves the region manager for the world associated with the location,
 * and verifies whether the protection flag is active for any applicable regions
 * corresponding to the specified coordinates.
 *
 * @return `true` if the location is within a protection region where the protection flag is active; `false` otherwise.
 */
fun Location.isInProtectionRegion(): Boolean {
    val manager = world.getRegionManagerOrNull() ?: return false

    val regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(this))
    return regions.testState(null, ProtectionFlagsRegistry.SURF_PROTECT)
}

/**
 * Determines if the current location is within a global region.
 *
 * A "global region" is defined as a location where no applicable regions are found
 * in the region manager associated with the world of the location. If the region
 * manager is unavailable or if no regions apply to the location, it is considered
 * part of the global region.
 *
 * @receiver The location to check.
 * @return `true` if the location is in a global region (i.e., no specific regions apply or the region manager is unavailable), `false` otherwise.
 */
fun Location.isGlobalRegion(): Boolean {
    val manager = world.getRegionManagerOrNull() ?: return true

    val regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(this))
    return regions.size() == 0
}

/**
 * Provides access to the `WorldEditWorld` associated with this `WorldEditLocation`.
 *
 * The `world` property retrieves the `extent` of the `WorldEditLocation` and casts it to
 * `WorldEditWorld`. If the casting fails (i.e., the `extent` is not a `WorldEditWorld`),
 * an error is thrown with a descriptive message.
 *
 * This property is particularly useful when working with WorldEdit APIs that differentiate
 * between generic extents and actual world-specific contexts.
 *
 * @receiver The `WorldEditLocation` instance for which the world is being accessed.
 * @return The `WorldEditWorld` corresponding to the extent of the location.
 * @throws IllegalStateException If the extent cannot be cast to `WorldEditWorld`.
 */
val WorldEditLocation.world: WorldEditWorld
    get() = this.extent as? WorldEditWorld ?: error("Extent is not a World: $extent")

val WorldEditLocation.bukkitWorld
    get() = Bukkit.getWorld(world.name)
        ?: error("Bukkit world not found for WorldEdit world: ${world.name}")

/**
 * Checks if the player is currently standing in the specified protected region.
 *
 * @param region The protected region to check against the player's current location.
 * @return True if the player is standing in the specified protected region, false otherwise.
 */
fun Player.standsInProtectedRegion(region: ProtectedRegion) =
    location.getProtectedRegions().any { it.id == region.id }


/**
 * Retrieves a set of protected regions that the current location falls into.
 *
 * @param withGlobalRegion Indicates whether the global region should be included in the results.
 * If set to true, the global region is included; otherwise, it is excluded. Defaults to false.
 * @return A set of protected regions that the location is part of. If `withGlobalRegion` is
 * false, the global region will not be included in the returned set.
 */
fun Location.getProtectedRegions(withGlobalRegion: Boolean = false): Set<ProtectedRegion> {
    val loc = BukkitAdapter.adapt(this)
    val regions = regionContainer.createQuery().getApplicableRegions(loc)

    return if (withGlobalRegion) {
        regions.regions
    } else {
        regions.regions.filterNotTo(mutableObjectSetOf()) { it.id == ProtectedRegion.GLOBAL_REGION }
    }
}

/**
 * Retrieves the block data at the specified block coordinates in this chunk snapshot.
 *
 * @param blockX The x-coordinate of the block (world coordinates).
 * @param blockY The y-coordinate of the block (height in the world).
 * @param blockZ The z-coordinate of the block (world coordinates).
 * @return The block data at the specified coordinates in this chunk snapshot.
 */
fun ChunkSnapshot.getBlockDataAt(
    blockX: Int,
    blockY: Int,
    blockZ: Int
): BlockData {
    return getBlockData(blockX and 15, blockY, blockZ and 15)
}

fun WorldEditLocation?.formatString() =
    if (this == null) "/" else "(${x.toInt()}, ${y.toInt()}, ${z.toInt()})"