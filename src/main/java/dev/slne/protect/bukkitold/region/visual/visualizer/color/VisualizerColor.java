package dev.slne.protect.bukkitold.region.visual.visualizer.color;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import dev.slne.surf.surfapi.core.api.util.BlockStateFactory;

public enum VisualizerColor {

  OWNING(BlockStateFactory.of(StateTypes.LIME_STAINED_GLASS)),
  MEMBER(BlockStateFactory.of(StateTypes.LIGHT_BLUE_STAINED_GLASS)),
  NOT_OWNING(BlockStateFactory.of(StateTypes.RED_STAINED_GLASS));

  private final WrappedBlockState blockState;

  /**
   * Create a new color
   *
   * @param blockState the block state
   */
  VisualizerColor(WrappedBlockState blockState) {
    this.blockState = blockState;
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
