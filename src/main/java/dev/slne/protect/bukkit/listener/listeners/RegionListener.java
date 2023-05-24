package dev.slne.protect.bukkit.listener.listeners;

import java.util.ArrayList;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.region.ProtectionUtils;

public class RegionListener implements Listener {

	@EventHandler
	public void onIgnite(BlockIgniteEvent event) {
		boolean valid = event.getCause().equals(IgniteCause.FLINT_AND_STEEL)
				|| event.getCause().equals(IgniteCause.FIREBALL);
		event.setCancelled(!(valid) && !(ProtectionUtils.isGlobalRegion(event.getBlock().getLocation())));
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block block : new ArrayList<>(event.blockList())) {
			for (ProtectedRegion protectedRegion : ProtectionUtils.getProtectedRegionsByLocation(block.getLocation())) {
				State state = protectedRegion.getFlag(Flags.OTHER_EXPLOSION);

				if (state == null) {
					state = State.DENY;
				}

				if (state.equals(State.DENY)) {
					event.blockList().remove(block);
				}
			}
		}
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		for (Block block : new ArrayList<>(event.blockList())) {
			for (ProtectedRegion protectedRegion : ProtectionUtils.getProtectedRegionsByLocation(block.getLocation())) {
				State state = protectedRegion.getFlag(Flags.OTHER_EXPLOSION);

				if (state == null) {
					state = State.DENY;
				}

				if (state.equals(State.DENY)) {
					event.blockList().remove(block);
				}
			}
		}
	}

}
