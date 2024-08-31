package dev.slne.protect.bukkit.region.visual.visualizer;

import com.google.common.flogger.FluentLogger;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.visualizer.visualizers.PolygonalProtectionVisualizer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.IOException;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProtectionVisualizerThread extends BukkitRunnable {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final ObjectList<ProtectionVisualizer<?>> visualizers;

  /**
   * Create a new visualizer thread
   */
  public ProtectionVisualizerThread() {
    this.visualizers = ObjectLists.synchronize(new ObjectArrayList<>());
  }

  @Override
  public void run() {
    for (final ProtectionVisualizer<?> visualizer : this.visualizers) {
      visualizer.update();
    }
  }

  /**
   * Start the visualizer thread
   */
  public void start() {
    this.runTaskTimerAsynchronously(BukkitMain.getInstance(), 0,
        ProtectionSettings.PROTECTION_VISUALIZER_UPDATE_INTERVAL * 20L);
  }

  /**
   * Stop the visualizer thread
   */
  public void stop() {
    try {
      if (!this.isCancelled()) {
        for (final ProtectionVisualizer<?> visualizer : this.visualizers) {
          visualizer.close();
        }

        this.cancel();
      }
    } catch (Exception exception) {
      // IGNORE
    }
  }

  /**
   * Add a visualizer to the task
   *
   * @param world           the world
   * @param protectedRegion the region
   * @param player          the player
   */
  public void addVisualizer(World world, ProtectedRegion protectedRegion, Player player) {
    this.addVisualizer(new PolygonalProtectionVisualizer(world, protectedRegion, player));
  }

  /**
   * Add a visualizer to the thread
   *
   * @param visualizer the visualizer
   */
  public void addVisualizer(ProtectionVisualizer<?> visualizer) {
    this.visualizers.add(visualizer);
  }

  /**
   * Get all visualizers
   *
   * @return the visualizers
   */
  public List<ProtectionVisualizer<? extends ProtectedRegion>> getVisualizers() {
    return visualizers;
  }

  /**
   * Get all visualizers for a player
   *
   * @param player the player
   * @return the visualizers
   */
  public List<ProtectionVisualizer<? extends ProtectedRegion>> getVisualizers(Player player) {
    return this.visualizers.stream().filter(visualizer -> visualizer.getPlayer().equals(player))
        .toList();
  }

  /**
   * Remove a visualizer from the thread
   *
   * @param visualizer the visualizer
   */
  public void closeVisualizer(ProtectionVisualizer<?> visualizer) {
    this.closeVisualizer(visualizer, true);
  }

  private void closeVisualizer(ProtectionVisualizer<?> visualizer, boolean remove) {
    if (remove) {
      this.visualizers.remove(visualizer);
    }

    try {
      visualizer.close();
    } catch (IOException e) {
      logger.atSevere().withCause(e).log("Failed to close visualizer");
    }
  }

  /**
   * Remove all visualizers for a player
   *
   * @param player the player
   */
  public void removeVisualizers(Player player) {
    final ObjectListIterator<ProtectionVisualizer<?>> iterator = visualizers.iterator();

    while (iterator.hasNext()) {
      final ProtectionVisualizer<?> visualizer = iterator.next();

      if (visualizer.getPlayer().equals(player)) {
        this.closeVisualizer(visualizer, false);
        iterator.remove();
      }
    }
  }
}
