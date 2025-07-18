package dev.slne.surf.protect.paper.region

import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.world.World
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.math.Mth

fun Location.getProtectionPricePerBlock(): PricePerBlockResult {
    val world = extent as World
    val spawn = world.spawnPosition.withY(0)
    val distance = toVector().toBlockPoint().withY(0).distance(spawn)

    if (distance < config.pricing.spawnProtectionPerBlock) {
        return PricePerBlockResult(Double.MAX_VALUE, distance)
    }

    return PricePerBlockResult(Mth.calculatePricePerBlock(distance), distance)
}

data class PricePerBlockResult(
    val pricePerBlock: Double,
    val spawnDistance: Double
)