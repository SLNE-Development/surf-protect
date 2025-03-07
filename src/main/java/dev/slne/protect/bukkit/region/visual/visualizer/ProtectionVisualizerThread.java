package dev.slne.protect.bukkit.region.visual.visualizer;

import com.google.common.flogger.FluentLogger;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.visualizer.visualizers.PolygonalProtectionVisualizer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.List;

public class ProtectionVisualizerThread {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  private final ObjectList<ProtectionVisualizer<?>> visualizers;
  private ScheduledTask task;
  private final Plugin plugin;

  // Konstruktor: Stellt sicher, dass 'plugin' und 'visualizers' initialisiert werden
  public ProtectionVisualizerThread(Plugin plugin) {
    this.plugin = plugin;
    this.visualizers = ObjectLists.synchronize(new ObjectArrayList<>());
  }

  /**
   * Führt den Update-Loop aus, der alle Visualizer updatet.
   */
  public void run() {
    for (final ProtectionVisualizer<?> visualizer : this.visualizers) {
      visualizer.update();
    }
  }

  /**
   * Startet den Scheduler mithilfe von Folia's GlobalRegionScheduler.
   */
  public void start() {
    task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, context -> run(), 20L, ProtectionSettings.PROTECTION_VISUALIZER_UPDATE_INTERVAL * 20L);
  }

  /**
   * Stoppt den Scheduler und schließt alle aktiven Visualizer.
   */
  public void stop() {
    try {
      if (task != null && !task.isCancelled()) {
        for (final ProtectionVisualizer<?> visualizer : this.visualizers) {
          visualizer.close();
        }
        task.cancel();
      }
    } catch (Exception exception) {
      // Fehler beim Stoppen ignorieren
    }
  }

  /**
   * Fügt einen Visualizer anhand der übergebenen Parameter hinzu.
   */
  public void addVisualizer(World world, ProtectedRegion protectedRegion, Player player) {
    this.addVisualizer(new PolygonalProtectionVisualizer(world, protectedRegion, player));
  }

  /**
   * Fügt einen Visualizer zur internen Liste hinzu.
   */
  public void addVisualizer(ProtectionVisualizer<?> visualizer) {
    this.visualizers.add(visualizer);
  }

  /**
   * Gibt alle aktiven Visualizer zurück.
   */
  public List<ProtectionVisualizer<? extends ProtectedRegion>> getVisualizers() {
    return visualizers;
  }

  /**
   * Gibt alle Visualizer eines bestimmten Spielers zurück.
   */
  public List<ProtectionVisualizer<? extends ProtectedRegion>> getVisualizers(Player player) {
    return this.visualizers.stream()
        .filter(visualizer -> visualizer.getPlayer().equals(player))
        .toList();
  }

  /**
   * Entfernt einen bestimmten Visualizer.
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
   * Entfernt alle Visualizer eines bestimmten Spielers.
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
