package dev.slne.protect.bukkit.listener.listeners;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import dev.slne.protect.bukkit.utils.ProtectionUtils;

public class RegionListener implements Listener {

	@EventHandler
	public void onIgnite(BlockIgniteEvent event) {
		boolean valid = event.getCause().equals(IgniteCause.FLINT_AND_STEEL)
				|| event.getCause().equals(IgniteCause.FIREBALL);
		event.setCancelled(!(valid) && !(ProtectionUtils.isGlobalRegion(event.getBlock().getLocation())));
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		event.blockList().removeAll(new ArrayList<>(event.blockList().stream().filter(block -> {
			return !ProtectionUtils.isGlobalRegion(block.getLocation());
		}).toList()));
	}

	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		event.blockList().removeAll(new ArrayList<>(event.blockList().stream().filter(block -> {
			return !ProtectionUtils.isGlobalRegion(block.getLocation());
		}).toList()));
	}

}
