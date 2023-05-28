package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;

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
        System.out.println("Visulizing " + this.visualizers.size() + " regions");

        for (ProtectionVisualizer<?> visualizer : new ArrayList<>(this.visualizers)) {
            visualizer.remove();
            visualizer.visualize();

            visualizer.visualizeLocations();
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
    public List<ProtectionVisualizer<?>> getVisualizers() {
        return visualizers;
    }

    /**
     * Get all visualizers for a player
     *
     * @param player the player
     * @return the visualizers
     */
    public List<ProtectionVisualizer<?>> getVisualizers(Player player) {
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
