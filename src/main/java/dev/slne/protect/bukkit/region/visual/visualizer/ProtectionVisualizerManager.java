package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionVisualizerManager {

    private Map<ProtectionUser, ProtectionVisualizer> visualizers;

    /**
     * Create a new visualizer task
     */
    public ProtectionVisualizerManager() {
        this.visualizers = new HashMap<>();
    }

    /**
     * Add a visualizer to the task
     *
     * @param protectionUser the protection user
     * @param visualizer     the visualizer
     */
    public void addVisualizer(ProtectionUser protectionUser, ProtectionVisualizer visualizer) {
        this.visualizers.put(protectionUser, visualizer);
    }

    /**
     * Remove all visualizers for a player
     *
     * @param player the player
     */
    public void removeVisualizer(Player player) {
        this.visualizers.remove(ProtectionUser.getProtectionUser(player));
    }

    /**
     * Returns the visualizer for a player
     *
     * @param protectionUser the protection user
     * @return the visualizer
     */
    public ProtectionVisualizer getVisualizer(ProtectionUser protectionUser) {
        return this.visualizers.get(protectionUser);
    }

    /**
     * Get the visualizers
     *
     * @return the visualizers
     */
    public Map<ProtectionUser, ProtectionVisualizer> getVisualizers() {
        return visualizers;
    }

}
