package dev.slne.surf.protect.paper.user

import dev.slne.surf.protect.paper.config.config


class ProtectionCooldownTracker(
    private val baseMs: Long = config.protectionModeBaseCooldownMs,
    private val maxMs: Long = config.protectionModeMaxCooldownMs
) {
    private var aborts = 0
    private var nextFree = 0L   // epoch-ms

    val onCooldown get() = System.currentTimeMillis() < nextFree
    val timeLeft get() = (nextFree - System.currentTimeMillis()).coerceAtLeast(0L)

    fun recordAbort() {
        aborts++
        // Exponential back‑off: base x 2^abortCount, clamped to max.
        val cd = (baseMs * (1L shl aborts)).coerceAtMost(maxMs)
        nextFree = System.currentTimeMillis() + cd
    }

    fun reset() {
        aborts = 0
        nextFree = 0L
    }
}