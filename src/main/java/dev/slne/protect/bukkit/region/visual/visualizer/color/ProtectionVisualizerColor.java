package dev.slne.protect.bukkit.region.visual.visualizer.color;

import dev.slne.protect.bukkit.BukkitMain;

public class ProtectionVisualizerColor {

    /**
     * Get a random color
     *
     * @return the color
     */
    public VisualizerColor getRandomColor() {
        return VisualizerColor.values()[BukkitMain.getRandom().nextInt(
                VisualizerColor.values().length)];
    }

    /**
     * All usable colors
     */
    public enum VisualizerColor {
        OWNING(5947),
        NOT_OWNING(5956),

        WHITE(5942),
        ORANGE(5943),
        MAGENTA(5944),
        LIGHT_BLUE(5945),
        YELLOW(5946),
        LIME(5947),
        PINK(5948),
        GRAY(5949),
        LIGHT_GRAY(5950),
        CYAN(5951),
        PURPLE(5952),
        BLUE(5953),
        BROWN(5954),
        GREEN(5955),
        RED(5956),
        BLACK(5957);

        private int id;

        /**
         * Create a new color
         *
         * @param id
         */
        VisualizerColor(int id) {
            this.id = id;
        }

        /**
         * Returns the id of the color
         *
         * @return the id
         */
        public int getId() {
            return id;
        }
    }
}
