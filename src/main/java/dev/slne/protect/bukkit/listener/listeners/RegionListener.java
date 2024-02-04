package dev.slne.protect.bukkit.listener.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.listener.PlayerMoveListener;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.GreetingFlag;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.Set;

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
            for (ProtectedRegion protectedRegion : ProtectionUtils.getProtectedRegionsByLocation(block.getLocation())) {
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
