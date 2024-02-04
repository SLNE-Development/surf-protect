package dev.slne.protect.bukkit.math;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.TemporaryProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class Mth {

    private Mth() {
    }

    public static double getRegionPrice(@NotNull ProtectedRegion region) {
        final com.sk89q.worldedit.util.Location worldeditLocation = region.getFlag(Flags.TELE_LOC);

        if (worldeditLocation == null) {
            return Double.MAX_VALUE;
        }

        final Location teleportLocation = BukkitAdapter.adapt(worldeditLocation);
        final double pricePerBlock = ProtectionUtils.getProtectionPricePerBlock(teleportLocation);

        return (double) ProtectionUtils.getArea(region) * pricePerBlock;
    }

    public static double getRegionRetailPrice(@NotNull ProtectedRegion region) {
        return getRegionPrice(region) * ProtectionSettings.RETAIL_MODIFIER;
    }

    public static EffectiveCostResult calculateEffectiveCost(Location center, TemporaryProtectionRegion temporaryRegion) {
        final double pricePerBlock = ProtectionUtils.getProtectionPricePerBlock(center);
        double effectiveCost = calculateProtectionPrice(temporaryRegion, pricePerBlock);

        return new EffectiveCostResult(Math.round(effectiveCost * 100.0) / 100.0, pricePerBlock);
    }

    public static double calculatePricePerBlock(double distance) {
        double calculation = -0.0016666666667 * distance + 12.3333333333;
        calculation = Math.round(calculation * 100.0) / 100.0;

        return Math.max(ProtectionSettings.PRICE_PER_BLOCK, calculation);
    }

    /**
     * Calculates the price for the given region
     *
     * @param region        the region
     * @param pricePerBlock the price per block
     *
     * @return the price
     */
    public static double calculateProtectionPrice(TemporaryProtectionRegion region, double pricePerBlock) {
        return region.getEffectiveArea() * pricePerBlock;
    }

    public static long calculateSecondsLeft(long playerCooldownMillis, int cooldownTimeSeconds) {
        long playerCooldownSeconds = playerCooldownMillis / 1000;
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        return (playerCooldownSeconds + cooldownTimeSeconds) - currentTimeSeconds;
    }

    public record EffectiveCostResult(double effectiveCost, double pricePerBlock) {
    }
}
