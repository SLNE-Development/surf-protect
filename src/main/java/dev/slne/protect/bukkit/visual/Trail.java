package dev.slne.protect.bukkit.visual;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.regions.RegionCreation;
import dev.slne.protect.bukkit.utils.ProtectionSettings;

public class Trail extends BukkitRunnable {
	private final Marker markerStart;
	private final Marker markerEnd;
	private final HashMap<Location, BlockData> blocks;
	private final RegionCreation regionCreation;
	private Player protectingPlayer;

	private Vector increase;
	private double distance;
	private Location currentLocation;
	private Location start;

	private boolean blockSet = false;
	private final AtomicBoolean started;
	private boolean drawBlocks;

	public Trail(Marker markerStart, Marker markerEnd, RegionCreation regionCreation) {
		this.markerStart = markerStart;
		this.markerEnd = markerEnd;
		this.regionCreation = regionCreation;

		this.started = new AtomicBoolean();
		this.blocks = new HashMap<Location, BlockData>();
	}

	public void init() {
		Location start = markerStart.getLocation();
		Location end = markerEnd.getLocation();

		end = end.clone().add(0.5, 0.5, 0.5);
		this.currentLocation = start = start.clone().add(0.5, 0.5, 0.5);
		this.start = start.clone();
		this.increase = new Vector(end.getX() - start.getX(), end.getY() - start.getY(), end.getZ() - start.getZ())
				.normalize().multiply(0.4);
		this.distance = this.start.distanceSquared(end);
		this.protectingPlayer = regionCreation.getProtectionUser().getBukkitPlayer();

		this.drawBlocks = markerStart.hasPreviousData() || markerEnd.hasPreviousData();
	}

	public void stopTask(boolean revertChanges) {
		if (this.started.get()) {
			this.cancel();
		}
		blockSet = true;

		if (revertChanges) {
			for (Location location : blocks.keySet()) {
				// resend packet for player
				BlockData blockData = location.getBlock().getBlockData();
				protectingPlayer.sendBlockChange(location, blockData);
				protectingPlayer.playEffect(location, Effect.STEP_SOUND, blockData.getMaterial());
			}
		} else {
			// finally set blocks
			// if not removing block
			for (Entry<Location, BlockData> b : blocks.entrySet()) {
				Location location = b.getKey();
				BlockData blockData = b.getValue();

				location.getBlock().setBlockData(blockData);

				for (Player p : Bukkit.getOnlinePlayers()) {
					p.playEffect(location, Effect.STEP_SOUND, blockData.getMaterial());
				}
			}
		}

	}

	public void start() {
		if (!started.getAndSet(true)) {
			this.init();
			this.runTaskTimerAsynchronously(BukkitMain.getInstance(), 0, 1);
		}
	}

	public double getDistance() {
		return distance;
	}

	public RegionCreation getRegionCreation() {
		return regionCreation;
	}

	@Override
	public void run() {
		for (int i = 0; i < 4; i++) {
			doFrame();
		}
	}

	protected void doFrame() {
		currentLocation.add(increase);

		if (currentLocation.distanceSquared(start) >= distance) {
			currentLocation = start.clone();
			blockSet = true;
		}
		drawBlocks();

		Color color = this.drawBlocks ? Color.BLUE : Color.RED;

		DustOptions dustOptions = new DustOptions(color, 1);
		World world = currentLocation.getWorld();
		world.spawnParticle(Particle.REDSTONE, currentLocation, 1, dustOptions);
	}

	private void drawBlocks() {
		if (!drawBlocks) {
			return;
		}
		if (blockSet) {
			return;
		}
		World world = currentLocation.getWorld();

		Location blockLocation = currentLocation.toBlockLocation();
		if (blocks.containsKey(blockLocation)) {
			return;
		}

		Block block = blockLocation.getBlock();
		if (block.getType() == Material.REDSTONE_TORCH) {
			return;
		}

		blockLocation.setY(world.getHighestBlockYAt(blockLocation, ProtectionSettings.PROTECTION_HEIGHTMAP) + 1);
		BlockData blockData = Bukkit.createBlockData(ProtectionSettings.TRAIL_MATERIAL);
		blocks.put(blockLocation, blockData);
		protectingPlayer.sendBlockChange(blockLocation, blockData);

		for (Player p : world.getPlayers()) {
			p.playEffect(blockLocation, Effect.STEP_SOUND, ProtectionSettings.TRAIL_MATERIAL);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Trail trail = (Trail) o;
		return Objects.equals(markerStart, trail.markerStart) && Objects.equals(markerEnd, trail.markerEnd)
				|| Objects.equals(markerStart, trail.markerEnd) && Objects.equals(markerEnd, trail.markerStart);
	}

	@Override
	public int hashCode() {
		return Objects.hash(markerStart, markerEnd);
	}
}
