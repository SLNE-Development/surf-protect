package dev.slne.surf.protect.paper.region.visual

import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.objectListOf
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlin.math.abs

/**
 * A branch‑lean, allocation‑light Quick‑Hull implementation that works on an
 * [ObjectList] of [Marker]s and returns the convex hull in counter‑clockwise
 * order. Uses *fastutil* collections to avoid boxing and minimise GC load.
 *
 * Time‑complexity: **O(n log n)** worst‑case, **O(n)** for random point sets.
 */
object QuickHull {
    /**
     * Computes the convex hull of the given marker list.
     *
     * @param input A list containing **at least three** distinct markers.
     * @return A new [ObjectList] that represents the convex hull. The caller
     *         owns the list; modifications will not affect the original input.
     */
    fun compute(input: ObjectArrayList<Marker>): ObjectArrayList<Marker> {
        if (input.size < 3) return input

        val points = mutableObjectListOf(input)
        val hull = mutableObjectListOf<Marker>()


        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minIdx = -1
        var maxIdx = -1

        for (i in 0 until input.size) {
            val point = input[i]
            val x = point.blockX
            if (x < minX) {
                minX = x
                minIdx = i
            }

            if (x > maxX) {
                maxX = x
                maxIdx = i
            }
        }

        val a = points[minIdx]
        val b = points[maxIdx]

        val hullPoints = objectListOf(a, b)
        hull.addAll(hullPoints)
        points.removeAll(hullPoints)

        val left = mutableObjectListOf<Marker>()
        val right = mutableObjectListOf<Marker>()

        for (point in points) {
            when (pointSide(a, b, point)) {
                -1 -> left.add(point)
                1 -> right.add(point)
            }
        }

        for (i in input.indices) {
            val marker = input[i]

            if (pointSide(a, b, marker) == -1) {
                left.add(marker)
            } else if (pointSide(a, b, marker) == 1) {
                right.add(marker)
            }
        }

        buildHull(a, b, right, hull)
        buildHull(b, a, left, hull)

        return hull
    }


    fun buildHull(
        a: Marker,
        b: Marker,
        set: ObjectArrayList<Marker>,
        hull: ObjectList<Marker>
    ) {
        val insertPosition = hull.indexOf(b)
        if (set.isEmpty()) return

        if (set.size == 1) {
            val point = set.pop()
            hull.add(insertPosition, point)
            return
        }

        var maxDist = Int.MIN_VALUE
        var idx = -1
        for (i in 0 until set.size) {
            val point = set[i]
            val distance = distance(a, b, point)

            if (distance > maxDist) {
                maxDist = distance
                idx = i
            }
        }

        val point = set[idx]
        set.removeAt(idx)
        hull.add(insertPosition, point)

        // Partition remaining points into two sets: left of AP and PB
        val leftAP = mutableObjectListOf<Marker>()
        val leftPB = mutableObjectListOf<Marker>()
        for (q in set) {
            when {
                pointSide(a, point, q) == 1 -> leftAP.add(q)
                pointSide(point, b, q) == 1 -> leftPB.add(q)
            }
        }

        buildHull(a, point, leftAP, hull)
        buildHull(point, b, leftPB, hull)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun distance(a: Marker, b: Marker, c: Marker): Int {
        val abx = b.blockX - a.blockX
        val abz = b.blockZ - a.blockZ
        val area = abx * (a.blockZ - c.blockZ) - abz * (a.blockX - c.blockX)
        return abs(area)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun pointSide(a: Marker, b: Marker, c: Marker): Int {
        val value = (b.blockX - a.blockX) * (c.blockZ - a.blockZ) -
                (b.blockZ - a.blockZ) * (c.blockX - a.blockX)

        return when {
            value > 0 -> 1
            value < 0 -> -1
            else -> 0
        }
    }
}
