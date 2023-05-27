package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class VisualizerTask {

    private List<ProtectionVisualizer> visualizers;

    /**
     * Create a new visualizer task
     */
    public VisualizerTask() {
        this.visualizers = new ArrayList<>();
    }

    /**
     * Add a visualizer to the task
     *
     * @param visualizer the visualizer
     */
    public void addVisualizer(ProtectionVisualizer visualizer) {
        this.visualizers.add(visualizer);
    }

    /**
     * Remove a visualizer from the task
     *
     * @param visualizer
     */
    public void removeVisualizer(ProtectionVisualizer visualizer) {
        visualizer.stopVisualizing();

        this.visualizers.remove(visualizer);
    }

    /**
     * Remove all visualizers for a player
     *
     * @param player the player
     */
    public void removeVisualizers(Player player) {
        for (ProtectionVisualizer visualizer : new ArrayList<>(this.visualizers)) {
            if (visualizer.getProtectionUser().getBukkitPlayer().equals(player)) {
                this.removeVisualizer(visualizer);
            }
        }
    }

    /**
     * Get the visualizers
     *
     * @return the visualizers
     */
    public List<ProtectionVisualizer> getVisualizers() {
        return visualizers;
    }

}
