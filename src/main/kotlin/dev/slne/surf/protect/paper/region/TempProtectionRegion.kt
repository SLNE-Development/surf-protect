package dev.slne.surf.protect.paper.region

import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.util.fixedVolume
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import org.bukkit.World

/**
 * Represents a temporary protection region
 *
 * A temporary protection region is a region that is not yet protected
 */
data class TempProtectionRegion(
    val world: World,
    val region: ProtectedRegion,
    val manager: RegionManager
) {
    val volume: Long get() = region.fixedVolume()
    var effectiveVolume: Long = volume
        set(value) {
            field = value.coerceAtLeast(0)
        }

    fun overlapsUnownedRegion(localPlayer: LocalPlayer): Boolean =
        manager.overlapsUnownedRegion(this.region, localPlayer)

    fun overlaps(other: ProtectedRegion?): Boolean {
        if (other == null) return false
        val manager = world.getRegionManagerOrNull() ?: return false
        val regionSet = manager.getApplicableRegions(region)

        if (regionSet.size() < 1) {
            return false
        }

        return if (regionSet.regions.contains(other)) regionSet.size() > 1 else regionSet.size() > 0
    }

    fun protect() {
        manager.addRegion(region)
    }
}
