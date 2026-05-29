package dev.slne.surf.protect.paper.region

import com.sk89q.worldedit.util.Location
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.math.Mth
import dev.slne.surf.protect.paper.util.bukkitWorld
import org.bukkit.World
import org.bukkit.util.Vector

fun Location.getProtectionPricePerBlock(): PricePerBlockResult {
    val spawns = when (this.bukkitWorld.environment) {
        World.Environment.NETHER -> {
            listOf(
                Vector(0.0, 0.0, 0.0),
                Vector(0.0, 0.0, -3125.0),
                Vector(3125.0, 0.0, -3125.0),
                Vector(-3125.0, 0.0, -3125.0),
                Vector(0.0, 0.0, 3125.0),
                Vector(3125.0, 0.0, 3125.0),
                Vector(-3125.0, 0.0, 3125.0),
                Vector(3125.0, 0.0, 0.0),
                Vector(-3125.0, 0.0, 0.0)
            )
        }

        World.Environment.THE_END -> {
            listOf(
                Vector(0.0, 0.0, 0.0)
            )
        }

        else -> {
            listOf(
                Vector(0.0, 0.0, 0.0),
                Vector(0.0, 0.0, -25000.0),
                Vector(25000.0, 0.0, -25000.0),
                Vector(-25000.0, 0.0, -25000.0),
                Vector(0.0, 0.0, 25000.0),
                Vector(25000.0, 0.0, 25000.0),
                Vector(-25000.0, 0.0, 25000.0),
                Vector(25000.0, 0.0, 0.0),
                Vector(-25000.0, 0.0, 0.0)
            )
        }
    }

    val pos = Vector(blockX.toDouble(), 0.0, blockZ.toDouble())

    val nearestSpawn = spawns.minBy {
        pos.distance(it)
    }

    val distance = pos.distance(nearestSpawn)

    if (distance < config.pricing.spawnProtectionPerBlock) {
        return PricePerBlockResult(Double.MAX_VALUE, distance)
    }

    val multiplier = when (this.bukkitWorld.environment) {
        World.Environment.NORMAL -> 1.0
        else -> 2.5
    }

    return PricePerBlockResult(
        Mth.calculatePricePerBlock(distance) * multiplier,
        distance
    )
}

data class PricePerBlockResult(
    val pricePerBlock: Double,
    val spawnDistance: Double
)