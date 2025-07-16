package dev.slne.surf.protect.paper.region.visual

import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.util.distanceSquared
import io.papermc.paper.math.Position
import org.bukkit.Color
import org.bukkit.Particle
import org.spongepowered.math.vector.Vector3d
import java.io.Closeable

data class Trail(
    private val markerStart: Marker,
    private val markerEnd: Marker,
    val protectionRegion: ProtectionRegion,
    private val isProtecting: Boolean
) : Closeable {
    private val startPos = markerStart.pos
    private val increase = Vector3d(
        markerEnd.blockX - startPos.x(),
        markerEnd.blockY - startPos.y(),
        markerEnd.blockZ - startPos.z()
    ).normalize().mul(0.4)

    private val distanceSquared = startPos.distanceSquared(markerEnd.pos)
    private val dustOptions = Particle.DustOptions(if (isProtecting) Color.RED else Color.AQUA, 1f)
    private var currentLocation: Position = startPos

    init {
        TrailRunTask.trackTrail(this)
    }

    override fun close() {
        TrailRunTask.untrackTrail(this)
    }

    fun tick() {
        repeat(3) {
            doFrame()
        }
    }

    private fun doFrame() {
        currentLocation = currentLocation.offset(increase.x(), increase.y(), increase.z())
        if (currentLocation.distanceSquared(startPos) >= distanceSquared) {
            currentLocation = startPos
        }

        val world = protectionRegion.player.world
        world.spawnParticle(
            Particle.DUST,
            currentLocation.offset(0.5, 0.5, 0.5).toLocation(world),
            1,
            dustOptions
        )
    }
}