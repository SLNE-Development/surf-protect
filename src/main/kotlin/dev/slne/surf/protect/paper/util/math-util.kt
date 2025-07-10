@file:Suppress("NOTHING_TO_INLINE")

package dev.slne.surf.protect.paper.util

/**
 * Returns the mathematical floor of this [Double] as an [Int], using a branchless approach.
 *
 * This method avoids conditional branching by leveraging the sign bit of the raw IEEE-754 representation.
 * For positive numbers and whole values, it behaves identically to a direct cast (`toInt()`).
 * For negative non-integer values, it subtracts 1 to ensure correct flooring behavior.
 *
 * Example:
 * ```
 * 3.7.fastFloorToInt()    // 3
 * -3.7.fastFloorToInt()   // -4
 * -2.0.fastFloorToInt()   // -2
 * ```
 *
 * @receiver The double value to floor.
 * @return The largest integer less than or equal to this value.
 */
inline fun Double.fastFloorToInt(): Int {
    val floor = toInt()
    return if (floor.toDouble() == this) floor else floor - (toRawBits() ushr 63).toInt()
}

fun getXFromChunkKey(key: Long): Int {
    return (key and 0xFFFF_FFFFL).toInt()
}

fun getZFromChunkKey(key: Long): Int {
    return (key ushr 32).toInt()
}
