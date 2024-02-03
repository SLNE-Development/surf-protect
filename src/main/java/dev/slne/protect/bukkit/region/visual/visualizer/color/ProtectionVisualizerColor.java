package dev.slne.protect.bukkit.region.visual.visualizer.color;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import dev.slne.protect.bukkit.BukkitMain;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

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
        OWNING(Material.LIME_STAINED_GLASS.createBlockData()),
        MEMBER(Material.ORANGE_STAINED_GLASS.createBlockData()),
        NOT_OWNING(Material.RED_STAINED_GLASS.createBlockData()),

        WHITE(Material.WHITE_STAINED_GLASS.createBlockData()),
        ORANGE(Material.ORANGE_STAINED_GLASS.createBlockData()),
        MAGENTA(Material.MAGENTA_STAINED_GLASS.createBlockData()),
        LIGHT_BLUE(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()),
        YELLOW(Material.YELLOW_STAINED_GLASS.createBlockData()),
        LIME(Material.LIME_STAINED_GLASS.createBlockData()),
        PINK(Material.PINK_STAINED_GLASS.createBlockData()),
        GRAY(Material.GRAY_STAINED_GLASS.createBlockData()),
        LIGHT_GRAY(Material.LIGHT_GRAY_STAINED_GLASS.createBlockData()),
        CYAN(Material.CYAN_STAINED_GLASS.createBlockData()),
        PURPLE(Material.PURPLE_STAINED_GLASS.createBlockData()),
        BLUE(Material.BLUE_STAINED_GLASS.createBlockData()),
        BROWN(Material.BROWN_STAINED_GLASS.createBlockData()),
        GREEN(Material.GREEN_STAINED_GLASS.createBlockData()),
        RED(Material.RED_STAINED_GLASS.createBlockData()),
        BLACK(Material.BLACK_STAINED_GLASS.createBlockData());

        private final WrappedBlockState blockState;

        /**
         * Create a new color
         *
         * @param blockState the block state
         */
        VisualizerColor(BlockData blockState) {
            this.blockState = SpigotConversionUtil.fromBukkitBlockData(blockState);
        }

        /**
         * Returns the id of the color
         *
         * @return the id
         */
        public WrappedBlockState getBlockState() {
            return blockState;
        }


    }
}
