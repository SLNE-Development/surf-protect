package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.visualizer.visualizers.PolygonalProtectionVisualizer;

public class ProtectionVisualizerThread extends BukkitRunnable {

    private List<ProtectionVisualizer<?>> visualizers;

    /**
     * Create a new visualizer thread
     */
    public ProtectionVisualizerThread() {
        this.visualizers = new ArrayList<>();
    }

    @Override
    public void run() {
        for (ProtectionVisualizer<?> visualizer : new ArrayList<>(this.visualizers)) {
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
                for (ProtectionVisualizer<?> visualizer : new ArrayList<>(this.visualizers)) {
                    visualizer.remove();
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
        /*
         * if (protectedRegion instanceof ProtectedCuboidRegion cuboidRegion) {
         * this.addVisualizer(new CuboidProtectionVisualizer(world, cuboidRegion,
         * player));
         * } else
         */ if (protectedRegion instanceof ProtectedPolygonalRegion polygonalRegion) {
            this.addVisualizer(new PolygonalProtectionVisualizer(world, polygonalRegion, player));
        }
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
        return new ArrayList<>(this.visualizers.stream().filter(visualizer -> visualizer.getPlayer().equals(player))
                .collect(Collectors.toList()));
    }

    /**
     * Remove a visualizer from the thread
     *
     * @param visualizer the visualizer
     */
    public void removeVisualizer(ProtectionVisualizer<?> visualizer) {
        this.visualizers.remove(visualizer);
    }

    /**
     * Remove all visualizers for a player
     *
     * @param player the player
     */
    public void removeVisualizers(Player player) {
        for (ProtectionVisualizer<?> visualizer : new ArrayList<>(this.visualizers)) {
            if (visualizer.getPlayer().equals(player)) {
                this.removeVisualizer(visualizer);
            }
        }
    }

}
