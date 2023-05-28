package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.region.visual.visualizer.visualizers.CuboidProtectionVisualizer;
import dev.slne.protect.bukkit.region.visual.visualizer.visualizers.PolygonalProtectionVisualizer;

public class ProtectionVisualizerManager {

    private ProtectionVisualizerThread thread;

    /**
     * Create a new visualizer task
     */
    public ProtectionVisualizerManager() {
        this.thread = new ProtectionVisualizerThread();
    }

    /**
     * Add a visualizer to the task
     *
     * @param world           the world
     * @param protectedRegion the region
     * @param player          the player
     */
    public void addVisualizer(World world, ProtectedRegion protectedRegion, Player player) {
        if (protectedRegion instanceof ProtectedCuboidRegion cuboidRegion) {
            this.addVisualizer(new CuboidProtectionVisualizer(world, cuboidRegion, player));
        } else if (protectedRegion instanceof ProtectedPolygonalRegion polygonalRegion) {
            this.addVisualizer(new PolygonalProtectionVisualizer(world, polygonalRegion, player));
        } else {
            throw new IllegalArgumentException("Region type not supported.");
        }
    }

    /**
     * Add a visualizer to the task
     *
     * @param visualizer the visualizer
     */
    public void addVisualizer(ProtectionVisualizer<?> visualizer) {
        this.thread.addVisualizer(visualizer);
    }

    /**
     * Remove all visualizers for a player
     *
     * @param player the player
     */
    public void removeVisualizer(Player player) {
        this.thread.removeVisualizers(player);
    }

    /**
     * Get all visualizers
     *
     * @return the visualizers
     */
    public List<ProtectionVisualizer<?>> getVisualizers() {
        return this.thread.getVisualizers();
    }

    /**
     * Get all visualizers for a player
     *
     * @param player the player
     * @return the visualizers
     */
    public List<ProtectionVisualizer<?>> getVisualizers(Player player) {
        return this.thread.getVisualizers(player);
    }

    /**
     * Start the visualizer task
     */
    public void start() {
        this.thread.start();
    }

    /**
     * Stop the visualizer task
     */
    public void stop() {
        this.thread.stop();
    }

}
