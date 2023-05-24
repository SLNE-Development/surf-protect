package dev.slne.protect.bukkit.region.visual;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.ProtectionRegion;

public class Trail extends BukkitRunnable {

	private final Marker markerStart;
	private final Marker markerEnd;

	private final ProtectionRegion protectionRegion;
	private Vector increase;
	private double distanceSquared;
	private Location currentLocation;

	private final AtomicBoolean started;
	private final boolean isProtecting;

	/**
	 * Creates a new trail
	 *
	 * @param markerStart      the marker start
	 * @param markerEnd        the marker end
	 * @param protectionRegion the protection region
	 */
	public Trail(Marker markerStart, Marker markerEnd, ProtectionRegion protectionRegion, boolean isProtecting) {
		this.markerStart = markerStart;
		this.markerEnd = markerEnd;
		this.protectionRegion = protectionRegion;
		this.isProtecting = isProtecting;

		this.started = new AtomicBoolean();
	}

	/**
	 * Inits trail values
	 */
	public void init() {
		Location startLocation = markerStart.getLocation().clone().add(0.5, 0.5, 0.5);
		Location endLocation = markerEnd.getLocation().clone().add(0.5, 0.5, 0.5);

		this.currentLocation = startLocation.clone();
		this.increase = new Vector(endLocation.getX() - startLocation.getX(), endLocation.getY() - startLocation.getY(),
				endLocation.getZ() - startLocation.getZ()).normalize().multiply(0.4);
		this.distanceSquared = startLocation.distanceSquared(endLocation);
	}

	/**
	 * Stops the trail task
	 */
	public void stopTask() {
		if (this.started.get()) {
			this.cancel();
		}
	}

	/**
	 * Starts the trail task
	 */
	public void start() {
		if (!started.getAndSet(true)) {
			this.init();
			this.runTaskTimerAsynchronously(BukkitMain.getInstance(), 0, 1);
		}
	}

	@Override
	public void run() {
		for (int i = 0; i < 4; i++) {
			doFrame();
		}
	}

	/**
	 * Returns the distance squared
	 *
	 * @return the distance squared
	 */
	public double getDistanceSquared() {
		return distanceSquared;
	}

	/**
	 * Returns the {@link ProtectionRegion}
	 *
	 * @return the {@link ProtectionRegion}
	 */
	public ProtectionRegion getProtectionRegion() {
		return protectionRegion;
	}

	/**
	 * Does a frame
	 */
	private void doFrame() {
		this.currentLocation.add(this.increase);
		Color color = this.isProtecting ? Color.GREEN : Color.AQUA;

		DustOptions dustOptions = new DustOptions(color, 1);
		World world = currentLocation.getWorld();
		world.spawnParticle(Particle.REDSTONE, currentLocation, 1, dustOptions);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		if (other == null || getClass() != other.getClass()) {
			return false;
		}

		Trail trail = (Trail) other;

		return Objects.equals(markerStart, trail.markerStart) && Objects.equals(markerEnd, trail.markerEnd)
				|| Objects.equals(markerStart, trail.markerEnd) && Objects.equals(markerEnd, trail.markerStart);
	}

	@Override
	public int hashCode() {
		return Objects.hash(markerStart, markerEnd);
	}
}
