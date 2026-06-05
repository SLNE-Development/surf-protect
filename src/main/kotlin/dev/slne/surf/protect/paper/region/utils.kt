package dev.slne.surf.protect.paper.region

import com.sk89q.worldedit.util.Location
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.math.Mth
import dev.slne.surf.protect.paper.util.bukkitWorld
import org.bukkit.World
import org.spongepowered.math.vector.Vector2l

private val spawnPositionsByEnvironment = mapOf(
    World.Environment.NETHER to listOf(
        Vector2l(0, 0),
        Vector2l(0, -3125),
        Vector2l(3125, -3125),
        Vector2l(-3125, -3125),
        Vector2l(0, 3125),
        Vector2l(3125, 3125),
        Vector2l(-3125, 3125),
        Vector2l(3125, 0),
        Vector2l(-3125, 0)
    ),
    World.Environment.THE_END to listOf(Vector2l.ZERO),
    World.Environment.NORMAL to listOf(
        Vector2l(0, 0),
        Vector2l(0, -25000),
        Vector2l(25000, -25000),
        Vector2l(-25000, -25000),
        Vector2l(0, 25000),
        Vector2l(25000, 25000),
        Vector2l(-25000, 25000),
        Vector2l(25000, 0),
        Vector2l(-25000, 0)
    )
)

fun Location.getProtectionPricePerBlock(): PricePerBlockResult {
    val environment = this.bukkitWorld.environment
    val spawns = spawnPositionsByEnvironment[environment] ?: return PricePerBlockResult.EMPTY
    val pos = Vector2l(blockX.toLong(), blockZ.toLong())

    val nearestSpawn = spawns.minBy { pos.distanceSquared(it) }
    val distance = pos.distance(nearestSpawn)

    if (!distance.isFinite()) {
        return PricePerBlockResult.EMPTY
    }

    if (distance < config.pricing.spawnProtectionPerBlock) {
        return PricePerBlockResult(Double.MAX_VALUE, distance)
    }

    val multiplier = config.pricing.environmentMultiplier[environment] ?: 1.0

    return PricePerBlockResult(
        Mth.calculatePricePerBlock(distance) * multiplier,
        distance
    )
}

data class PricePerBlockResult(
    val pricePerBlock: Double,
    val spawnDistance: Double
) {
    companion object {
        val EMPTY = PricePerBlockResult(Double.MAX_VALUE, Double.MAX_VALUE)
    }
}