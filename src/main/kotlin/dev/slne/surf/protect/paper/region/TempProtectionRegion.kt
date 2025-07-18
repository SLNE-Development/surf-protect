package dev.slne.surf.protect.paper.region

import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.util.fixedVolume
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import org.bukkit.World

/**
 * Represents a temporary protection region in a specific world.
 *
 * This class provides utilities to manipulate and evaluate temporary protective
 * regions, such as checking overlaps, managing region protection, and computing
 * volumes in a world. The region's effective volume can also be adjusted if required,
 * ensuring it is always non-negative.
 *
 * @property world The world in which the region exists.
 * @property region The underlying protected region being managed.
 * @property manager The regional manager handling the region's context.
 */
data class TempProtectionRegion(
    val world: World,
    val region: ProtectedRegion,
    val manager: RegionManager
) {
    /**
     * Represents the calculated volume of the protected region as a 2D area.
     *
     * @return The volume of the region as a [Long].
     */
    val volume: Long get() = region.fixedVolume()

    /**
     * Represents the effective volume of the temporary protection region.
     *
     * The effective volume is derived from the original volume of the protected region
     * but can be modified as needed. Any value assigned to this property is automatically
     * coerced to be non-negative to ensure consistency and correctness of region protection
     * calculations.
     *
     * The effective volume is utilized for operations such as calculating protection
     * pricing or evaluating the impact of the region in the context of other overlapping
     * or adjacent regions.
     */
    var effectiveVolume: Long = volume
        set(value) {
            field = value.coerceAtLeast(0)
        }


    /**
     * Checks if the current temporary protection region overlaps with any region not owned
     * by the specified player.
     *
     * @param localPlayer The local player whose ownership status is considered when evaluating overlap.
     * @return `true` if the region overlaps with any unowned region for the given player, `false` otherwise.
     */
    fun overlapsUnownedRegion(localPlayer: LocalPlayer): Boolean =
        manager.overlapsUnownedRegion(this.region, localPlayer)

    /**
     * Checks if the current protected region overlaps with the specified `other` region.
     *
     * An overlap is determined based on the regions applicable to the current region within
     * the world. When `other` is null, or if no region manager is available, the method
     * returns false.
     *
     * @param other The protected region to check for overlap. Can be null.
     * @return True if the regions overlap; false otherwise.
     */
    fun overlaps(other: ProtectedRegion?): Boolean {
        if (other == null) return false
        val manager = world.getRegionManagerOrNull() ?: return false
        val regionSet = manager.getApplicableRegions(region)

        if (regionSet.size() < 1) {
            return false
        }

        return if (regionSet.regions.contains(other)) regionSet.size() > 1 else regionSet.size() > 0
    }

    /**
     * Protects the associated region by adding it to the regional manager for tracking
     * and enforcement. This ensures the region is registered and managed as part of the
     * defined protection system.
     */
    fun protect() {
        manager.addRegion(region)
    }
}
