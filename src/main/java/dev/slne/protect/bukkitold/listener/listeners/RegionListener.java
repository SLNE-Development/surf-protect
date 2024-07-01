package dev.slne.protect.bukkitold.listener.listeners;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkitold.region.ProtectionUtils;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;

public class RegionListener implements Listener {

  @EventHandler
  public void onIgnite(BlockIgniteEvent event) {
    IgniteCause cause = event.getCause();

    if (cause.equals(IgniteCause.FLINT_AND_STEEL) || cause.equals(IgniteCause.FIREBALL)) {
      return; // Allow
    }

    if (ProtectionUtils.isGlobalRegion(event.getBlock().getLocation())) {
      return; // Allow
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onEntityExplode(EntityExplodeEvent event) {
    performBlockRemove(event.blockList());
  }

  @EventHandler
  public void onBlockExplode(BlockExplodeEvent event) {
    performBlockRemove(event.blockList());
  }

  private void performBlockRemove(List<Block> blocks) {
    blocks.removeIf(block -> {
      for (ProtectedRegion protectedRegion : ProtectionUtils.getProtectedRegionsByLocation(
          block.getLocation())) {
        State state = protectedRegion.getFlag(Flags.OTHER_EXPLOSION);

        if (state == null) {
          state = State.DENY;
        }

        if (state.equals(State.DENY)) {
          return true;
        }
      }

      return false;
    });
  }
}
