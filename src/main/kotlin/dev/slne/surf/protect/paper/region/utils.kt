package dev.slne.surf.protect.paper.region

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.util.Location
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.math.Mth

private val spawns = listOf(
    BlockVector3(0, 0, 0),
    BlockVector3(0, 0, -25000),
    BlockVector3(25000, 0, -25000),
    BlockVector3(-25000, 0, -25000),
    BlockVector3(0, 0, 25000),
    BlockVector3(25000, 0, 25000),
    BlockVector3(-25000, 0, 25000),
    BlockVector3(25000, 0, 0),
    BlockVector3(-25000, 0, 0)
)

fun Location.getProtectionPricePerBlock(): PricePerBlockResult {
    val pos = toVector().toBlockPoint().withY(0)

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