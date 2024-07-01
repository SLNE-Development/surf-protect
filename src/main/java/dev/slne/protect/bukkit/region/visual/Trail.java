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
	private Location startLocation;
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
		Location end = markerEnd.getLocation().clone();

		this.startLocation = markerStart.getLocation().clone();
		this.currentLocation = this.startLocation.clone();
		this.increase = new Vector(end.getX() - this.startLocation.getX(), end.getY() - this.startLocation.getY(),
				end.getZ() - this.startLocation.getZ()).normalize().multiply(0.4);
		this.distanceSquared = this.startLocation.distanceSquared(end);
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

		if (this.currentLocation.distanceSquared(this.startLocation) >= this.distanceSquared) {
			this.currentLocation = this.startLocation.clone();
		}

		Color color = this.isProtecting ? Color.RED : Color.AQUA;

		DustOptions dustOptions = new DustOptions(color, 1);
		World world = this.currentLocation.getWorld();
		world.spawnParticle(Particle.DUST, this.currentLocation.clone().add(0.5, 0.5, 0.5), 1, dustOptions);
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
