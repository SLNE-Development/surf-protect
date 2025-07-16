package dev.slne.surf.protect.paper.region.visual

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object TrailRunTask {
    private val tasks = mutableObjectSetOf<Trail>()

    init {
        launchUpdateTask()
    }

    fun trackTrail(trail: Trail) {
        tasks.add(trail)
    }

    fun untrackTrail(trail: Trail) {
        tasks.remove(trail)
    }

    fun tickTrails() {
        for (trail in tasks) {
            trail.tick()
        }
    }

    private fun launchUpdateTask() = plugin.launch {
        while (isActive) {
            delay(1.ticks)
            tickTrails()
        }
    }
}