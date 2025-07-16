package dev.slne.surf.protect.paper.math

import com.sk89q.worldedit.util.Location
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.region.TempProtectionRegion
import dev.slne.surf.protect.paper.region.getProtectionPricePerBlock
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings
import dev.slne.surf.protect.paper.util.fixedVolume
import java.util.function.BiConsumer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

object Mth {
    private const val PRICE_GRADIENT = -0.0016666666667
    private const val PRICE_INTERCEPT = 12.3333333333

    @JvmStatic
    fun getRegionPrice(region: ProtectedRegion): Double {
        val worldeditLocation = region.getFlag(Flags.TELE_LOC) ?: return Double.MAX_VALUE
        val (pricePerBlock) = worldeditLocation.getProtectionPricePerBlock()

        return region.fixedVolume() * pricePerBlock
    }

    @JvmStatic
    fun getRegionRetailPrice(region: ProtectedRegion): Double {
        return getRegionPrice(region) * config.protection.retailModifier
    }

    @JvmStatic
    fun calculateEffectiveCost(
        center: Location,
        temporaryRegion: TempProtectionRegion
    ): EffectiveCostResult {
        val (pricePerBlock, spawnDistance) = center.getProtectionPricePerBlock()
        val rawCost = calculateProtectionPrice(temporaryRegion, pricePerBlock)

        return EffectiveCostResult(
            (rawCost * 100.0).roundToInt() / 100.0,
            pricePerBlock,
            spawnDistance
        )
    }

    @JvmStatic
    fun calculatePricePerBlock(distance: Double): Double {
        val raw = PRICE_GRADIENT * distance + PRICE_INTERCEPT
        val rounded = (raw * 100.0).roundToInt() / 100.0
        return max(config.pricing.minPerBlock, rounded)
    }

    inline fun calculateProtectionPrice(
        region: TempProtectionRegion,
        pricePerBlock: Double
    ): Double {
        return region.effectiveVolume * pricePerBlock
    }

    fun walkCoordinatesAToB(
        x1: Int, z1: Int,
        x2: Int, z2: Int,
        consumer: (Int, Int) -> Unit
    ) {
        var x1 = x1
        var z1 = z1
        val dx = abs(x2 - x1)
        val dz = abs(z2 - z1)

        val sx = if (x1 < x2) 1 else -1
        val sz = if (z1 < z2) 1 else -1

        var err = dx - dz

        while (true) {
            consumer(x1, z1)

            if (x1 == x2 && z1 == z2) break
            val e2 = err shl 1
            if (e2 > -dz) {
                err -= dz
                x1 += sx
            }

            if (e2 < dx) {
                err += dx
                z1 += sz
            }
        }
    }

    @Deprecated(
        message = "Use the Kotlin lambda version instead.",
        replaceWith = ReplaceWith("walkCoordinatesAToB(x1, z1, x2, z2, consumer::accept)")
    )
    @JvmStatic
    fun walkCoordinatesAToB(
        x1: Int, z1: Int,
        x2: Int, z2: Int,
        consumer: BiConsumer<Int, Int>
    ) = walkCoordinatesAToB(x1, z1, x2, z2) { x, z -> consumer.accept(x, z) }

    data class EffectiveCostResult(
        val effectiveCost: Double,
        val pricePerBlock: Double,
        val spawnDistance: Double
    )
}