package dev.slne.surf.protect.paper.region.structure

import com.sk89q.worldguard.protection.regions.ProtectedRegion

/**
 * The 2D outline of a region, used to decide which block columns belong to its area.
 *
 * A column counts as inside when its center lies inside the polygon spanned by the region points.
 * This matches how the protection area is measured (shoelace formula over the points), while
 * [ProtectedRegion.contains] also includes the blocks on the far border, which the area does not.
 */
class RegionPolygon(region: ProtectedRegion) {
    private val xs = region.points.map { it.x().toDouble() }.toDoubleArray()
    private val zs = region.points.map { it.z().toDouble() }.toDoubleArray()

    fun containsColumn(x: Int, z: Int): Boolean {
        val px = x + 0.5
        val pz = z + 0.5
        var inside = false
        var j = xs.size - 1
        for (i in xs.indices) {
            if ((zs[i] > pz) != (zs[j] > pz) &&
                px < (xs[j] - xs[i]) * (pz - zs[i]) / (zs[j] - zs[i]) + xs[i]
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
