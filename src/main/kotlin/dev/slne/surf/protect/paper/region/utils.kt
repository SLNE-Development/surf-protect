package dev.slne.surf.protect.paper.region

import com.sk89q.worldedit.util.Location
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.math.Mth
import org.bukkit.util.Vector

fun Location.getProtectionPricePerBlock(): PricePerBlockResult {
    val spawns = listOf(
        Vector(0, 0, 0),
        Vector(0, 0, -25000),
        Vector(25000, 0, -25000),
        Vector(-25000, 0, -25000),
        Vector(0, 0, 25000),
        Vector(25000, 0, 25000),
        Vector(-25000, 0, 25000),
        Vector(25000, 0, 0),
        Vector(-25000, 0, 0)
    )

    val pos = Vector(blockX.toDouble(), 0.0, blockZ.toDouble())

    val nearestSpawn = spawns.minBy {
        pos.distance(it)
    }

    val distance = pos.distance(nearestSpawn)

    if (distance < config.pricing.spawnProtectionPerBlock) {
        return PricePerBlockResult(Double.MAX_VALUE, distance)
    }

    return PricePerBlockResult(Mth.calculatePricePerBlock(distance), distance)
}

data class PricePerBlockResult(
    val pricePerBlock: Double,
    val spawnDistance: Double
)