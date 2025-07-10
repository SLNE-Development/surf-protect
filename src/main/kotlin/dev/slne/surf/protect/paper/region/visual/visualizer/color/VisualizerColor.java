package dev.slne.surf.protect.paper.region.visual.visualizer.color;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public enum VisualizerColor {

  OWNING(Material.LIME_STAINED_GLASS.createBlockData()),
  MEMBER(Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()),
  NOT_OWNING(Material.RED_STAINED_GLASS.createBlockData());

  private final BlockData blockState;

  /**
   * Create a new color
   *
   * @param blockState the block state
   */
  VisualizerColor(BlockData blockState) {
    this.blockState = blockState;
  }

  /**
   * Returns the id of the color
   *
   * @return the id
   */
  public BlockData getBlockData() {
    return blockState;
  }
}
