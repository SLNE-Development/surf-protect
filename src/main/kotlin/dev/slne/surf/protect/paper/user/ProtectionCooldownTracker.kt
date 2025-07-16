package dev.slne.surf.protect.paper.user

import dev.slne.surf.protect.paper.config.config
import kotlin.math.pow
import kotlin.math.roundToLong


class ProtectionCooldownTracker(
    private val baseMs: Long = config.cooldown.baseMs,
    private val maxMs: Long = config.cooldown.maxMs,
    private val targetAbortsToMax: Int = 10,
    private val startCooldown: Int = 3
) {
    private var aborts = 0
    private var nextFree = 0L   // epoch-ms

    val onCooldown get() = System.currentTimeMillis() < nextFree
    val timeLeft get() = (nextFree - System.currentTimeMillis()).coerceAtLeast(0L)

//    fun recordAbort() {
//        aborts++
//        // Exponential back‑off: base x 2^abortCount, clamped to max.
//        val cd = (baseMs * (1L shl aborts)).coerceAtMost(maxMs)
//        nextFree = System.currentTimeMillis() + cd
//    }

    fun recordAbort() {
        aborts++
        if (startCooldown > aborts) return

        val ratio = maxMs.toDouble() / baseMs
        val factor = ratio.pow(aborts.toDouble() / (targetAbortsToMax + startCooldown))
        val cd = (baseMs * factor).roundToLong().coerceAtMost(maxMs)

        nextFree = System.currentTimeMillis() + cd
    }


    fun reset() {
        aborts = 0
        nextFree = 0L
    }
}