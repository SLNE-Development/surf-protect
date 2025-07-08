package dev.slne.protect.paper.math;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.paper.region.ProtectionUtils;
import dev.slne.protect.paper.region.TemporaryProtectionRegion;
import dev.slne.protect.paper.region.settings.ProtectionSettings;
import java.util.function.BiConsumer;
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

  public static EffectiveCostResult calculateEffectiveCost(Location center,
      TemporaryProtectionRegion temporaryRegion) {
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
   * @return the price
   */
  public static double calculateProtectionPrice(TemporaryProtectionRegion region,
      double pricePerBlock) {
    return region.getEffectiveArea() * pricePerBlock;
  }

  public static long calculateSecondsLeft(long playerCooldownMillis, int cooldownTimeSeconds) {
    long playerCooldownSeconds = playerCooldownMillis / 1000;
    long currentTimeSeconds = System.currentTimeMillis() / 1000;

    return (playerCooldownSeconds + cooldownTimeSeconds) - currentTimeSeconds;
  }

  public static void walkCoordinatesAToB(int x1, int z1, int x2, int z2,
      BiConsumer<Integer, Integer> consumer) {
    int dx = Math.abs(x2 - x1);
    int dz = Math.abs(z2 - z1);

    int sx = x1 < x2 ? 1 : -1;
    int sz = z1 < z2 ? 1 : -1;

    int err = dx - dz;

    while (true) {
      consumer.accept(x1, z1);

      if (x1 == x2 && z1 == z2) {
        break;
      }

      int e2 = 2 * err;

      if (e2 > -dz) {
        err -= dz;
        x1 += sx;
      }

      if (e2 < dx) {
        err += dx;
        z1 += sz;
      }
    }
  }

  public record EffectiveCostResult(double effectiveCost, double pricePerBlock) {

  }
}
