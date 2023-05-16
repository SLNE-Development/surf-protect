package dev.slne.protect.bukkit.regions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import dev.slne.protect.bukkit.utils.ProtectionUtils;
import dev.slne.protect.bukkit.visual.Marker;
import dev.slne.protect.bukkit.visual.Trail;
import net.kyori.adventure.text.Component;

public class RegionCreation {

	private final Location startLocation;
	private final ProtectedRegion expandingProtection;
	private final ProtectionUser protectionUser;
	private TemporaryRegion temporaryRegion;
	private final double pricePerBlock;
	private List<Marker> boundingMarkers;
	private final List<Marker> markers;
	private final HashSet<Trail> trails;
	private final int maxMarkerCount = ProtectionSettings.MARKERS;
	private final ItemStack[] startingInventoryContent;

	public RegionCreation(ProtectionUser protectionUser, ProtectedRegion expandingProtection) {
		this.protectionUser = protectionUser;
		this.expandingProtection = expandingProtection;
		this.pricePerBlock = ProtectionSettings.PRICE_PER_BLOCK;
		this.startLocation = protectionUser.getBukkitPlayer().getLocation();
		this.startingInventoryContent = protectionUser.getBukkitPlayer().getInventory().getContents().clone();

		this.boundingMarkers = new ArrayList<Marker>();
		this.markers = new ArrayList<Marker>();
		this.trails = new HashSet<>();
	}

	public void setExpandingMarkers() {
		if (this.isExpandingRegion()) {
			// set markers
			World world = protectionUser.getBukkitPlayer().getWorld();
			List<BlockVector2> vector2s = expandingProtection.getPoints();
			for (BlockVector2 vector : vector2s) {
				Location location = new Location(world, vector.getX(), 0, vector.getZ());
				location.setY(world.getHighestBlockYAt(location, ProtectionSettings.PROTECTION_HEIGHTMAP) + 1);
				createMarker(location.getBlock(), null);
			}
		}
	}

	public int getMaxMarkerCount() {
		int expandingPoints = 0;
		if (expandingProtection != null) {
			expandingPoints = expandingProtection.getPoints().size();
		}
		return maxMarkerCount + expandingPoints;
	}

	public int getCurrentMarkerCount() {
		return this.markers.size();
	}

	public int getMarkerCountLeft() {
		return getMaxMarkerCount() - getCurrentMarkerCount();
	}

	public ProtectedRegion getExpandingProtection() {
		return expandingProtection;
	}

	public Location getStartLocation() {
		return startLocation;
	}

	public boolean isExpandingRegion() {
		return expandingProtection != null;
	}

	public List<Marker> getBoundingMarkers() {
		return boundingMarkers;
	}

	public TemporaryRegion getTemporaryRegion() {
		return temporaryRegion;
	}

	public void setTemporaryRegion(TemporaryRegion temporaryRegion) {
		this.temporaryRegion = temporaryRegion;
	}

	public ProtectionUser getProtectionUser() {
		return protectionUser;
	}

	protected double calculatePrice(TemporaryRegion region) {
		return region.getEffectiveArea() * pricePerBlock;
	}

	/**
	 * Checks if the given markers can form a temporary region
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	protected RegionCreationState offerAccepting() throws InterruptedException, ExecutionException {
		if (boundingMarkers.size() < 4) {
			protectionUser.getBukkitPlayer()
					.sendMessage(MessageManager.prefix()
							.append(Component.text("Du musst mindestens ", MessageManager.ERROR))
							.append(Component.text((4 - boundingMarkers.size()), MessageManager.VARIABLE_VALUE)
									.append(Component.text(" weitere Marker platzieren.", MessageManager.ERROR))));
			temporaryRegion = null;
			return RegionCreationState.MORE_MARKERS_NEEDED;
		}

		List<BlockVector2> vectors = new ArrayList<BlockVector2>();
		for (Marker ma : boundingMarkers) {
			vectors.add(BlockVector2.at(ma.getLocation().getX(), ma.getLocation().getZ()));
		}
		Player player = protectionUser.getBukkitPlayer();
		RegionManager manager = ProtectionUtils.getRegionManager(player.getWorld());

		ProtectedRegion region;
		if (this.isExpandingRegion()) {
			region = new ProtectedPolygonalRegion(expandingProtection.getId(), vectors, ProtectionSettings.MIN_Y_WORLD,
					ProtectionSettings.MAX_Y_WORLD);
			region.copyFrom(expandingProtection);
		} else {
			String name = player.getName() + "-" + RandomStringUtils.randomAlphabetic(5).toUpperCase();
			region = new ProtectedPolygonalRegion(name, vectors, ProtectionSettings.MIN_Y_WORLD,
					ProtectionSettings.MAX_Y_WORLD);
			region.getOwners().addPlayer(protectionUser.getLocalPlayer());
			region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(boundingMarkers.get(0).getLocation()));
		}
		TemporaryRegion temporaryRegion = new TemporaryRegion(region, manager);
		long area = temporaryRegion.getArea();

		if (this.isExpandingRegion()) {
			temporaryRegion.setEffectiveArea(area - ProtectionUtils.getArea(expandingProtection));
		} else {
			temporaryRegion.setEffectiveArea(area);
		}

		if (temporaryRegion.overlaps(expandingProtection)) {
			protectionUser.getBukkitPlayer().sendMessage(MessageManager.prefix().append(Component
					.text("Die markierte Fläche kollidiert mit einem anderen Grundstück.", MessageManager.ERROR)));
			return RegionCreationState.OVERLAPPING;
		} else if (area <= ProtectionSettings.AREA_MIN_BLOCKS) {
			protectionUser.getBukkitPlayer().sendMessage(MessageManager.prefix()
					.append(Component.text("Das Grundstück ist zu klein.", MessageManager.ERROR)));
			return RegionCreationState.TOO_SMALL;
		} else if (area > ProtectionSettings.AREA_MAX_BLOCKS) {
			protectionUser.getBukkitPlayer().sendMessage(MessageManager.prefix()
					.append(Component.text("Das Grundstück ist zu groß.", MessageManager.ERROR)));
			return RegionCreationState.TOO_LARGE;
		}

		double effectiveCost = this.calculatePrice(temporaryRegion);

		if (false/*
					 * !(protectionUser.getBukkitPlayer().hasEnoughCurrency(Currency.
					 * getDefaultCurrency(), effectiveCost))
					 */) {
			protectionUser.sendMessage(MessageManager.prefix());
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Das Grundstück steht zum Verkauf", MessageManager.SUCCESS)));
			protectionUser.sendMessage(MessageManager.prefix());
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Fläche: ", MessageManager.VARIABLE_KEY)
							.append(Component.text(area, MessageManager.VARIABLE_VALUE)
									.append(Component.text(" Blöcke", MessageManager.VARIABLE_VALUE)))));
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Kaufpreis: ", MessageManager.VARIABLE_KEY)
							.append(Component.text(effectiveCost, MessageManager.VARIABLE_VALUE)
									.append(Component.text(" €", MessageManager.VARIABLE_VALUE)))));
			protectionUser.sendMessage(MessageManager.prefix().append(Component
					.text("Du hast nicht genügend Geld um dieses Grundstück zu kaufen", MessageManager.ERROR)));
		} else {
			protectionUser.sendMessage(MessageManager.prefix());
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Das Grundstück steht zum Verkauf", MessageManager.SUCCESS)));
			protectionUser.sendMessage(MessageManager.prefix());
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Fläche: ", MessageManager.VARIABLE_KEY)
							.append(Component.text(area, MessageManager.VARIABLE_VALUE)
									.append(Component.text(" Blöcke", MessageManager.VARIABLE_VALUE)))));
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Kaufpreis: ", MessageManager.VARIABLE_KEY)
							.append(Component.text(effectiveCost, MessageManager.VARIABLE_VALUE)
									.append(Component.text(" €", MessageManager.VARIABLE_VALUE)))));
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text(
							"Wenn du das Grundstück kaufen möchtest, nutze den Bestätigungsknopf in deiner Hotbar",
							MessageManager.INFO)));

			protectionUser.sendMessage(MessageManager.prefix());
		}

		this.setTemporaryRegion(temporaryRegion);
		return RegionCreationState.SUCCESS;
	}

	/**
	 * Finished the protection
	 *
	 * @return if successful
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public boolean finishProtection() throws InterruptedException, ExecutionException {
		if (temporaryRegion == null) {
			offerAccepting();
			return false;
		}

		if (temporaryRegion.overlapsUnownedRegion(protectionUser.getLocalPlayer())) {
			protectionUser.getBukkitPlayer().sendMessage(MessageManager.prefix().append(Component
					.text("Die markierte Fläche kollidiert mit einem anderen Grundstück.", MessageManager.ERROR)));
			return false;
		}

		double effectiveCost = this.calculatePrice(temporaryRegion);
		if (false /*
					 * !(protectionUser.getBukkitPlayer().hasEnoughCurrency(Currency.
					 * getDefaultCurrency(), effectiveCost))
					 */) {
			protectionUser.getBukkitPlayer()
					.sendMessage(Component.text().append(MessageManager.prefix()).append(Component
							.text("Du kannst dir dieses Grundstück leider nicht leisten.", MessageManager.ERROR))
							.build());
			return false;
		}

		protectionUser.getBukkitPlayer().sendMessage(Component.text().append(MessageManager.prefix())
				.append(Component.text("Dein Grundstück wurde erfolgreich erstellt.", MessageManager.SUCCESS)).build());

		// Transaction creationTransaction = new Transaction(null,
		// protectionUser.getBukkitPlayer(),
		// Currency.getDefaultCurrency(), -effectiveCost,
		// ProtectionTransactionCause.NEW_PROTECTION.name());
		// creationTransaction.shouldNotify(false);

		// protectionUser.sendMessage(getTransactionReceivedComponent(creationTransaction));

		// protectionUser.getBukkitPlayer().addTransaction(creationTransaction).get();
		this.temporaryRegion.protect();

		this.removeAllMarkers(false);
		protectionUser.resetRegionCreation();
		return true;
	}

	/**
	 * Canceles the protection
	 */
	public void cancelProtection() {
		this.removeAllMarkers(true);

		protectionUser.getBukkitPlayer().sendMessage(Component.text().append(MessageManager.prefix())
				.append(Component.text("Du hast den ProtectionMode verlassen.", MessageManager.ERROR)).build());
		protectionUser.resetRegionCreation();
	}

	/**
	 * Removes all markers
	 *
	 * @param revertChanges if the markers should be reverted
	 */
	public void removeAllMarkers(boolean revertChanges) {
		while (!markers.isEmpty()) {
			removeMarker0(markers.get(0), revertChanges);
		}
		calculateBoundingMarkers(null);
		handleTrails(revertChanges);
	}

	/**
	 * Called when a new marker is created
	 *
	 * @param block        the block of the location
	 * @param previousData the previous data in that location
	 * @return the new marker or null, if marker could not be created
	 */
	public Marker createMarker(Block block, BlockData previousData) {
		Marker marker = new Marker(this, block.getLocation(), previousData);
		calculateBoundingMarkers(marker);

		if (!boundingMarkers.contains(marker)) {
			// marker is not bounding
			return null;
		}

		if (previousData != null) {
			try {
				if (offerAccepting().equals(RegionCreationState.OVERLAPPING)) {
					// recalculate
					this.removeMarker(marker, true); // removeAllMarkers = old
					this.handleTrails(true);
					calculateBoundingMarkers(marker);
					return null;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

		markers.add(marker);
		handleTrails(true);
		return marker;
	}

	private void calculateBoundingMarkers(Marker marker) {
		ArrayList<Marker> newMarkers = new ArrayList<>(this.markers);
		if (marker != null) {
			newMarkers.add(marker);
		}

		boundingMarkers = new QuickHull().quickHull(newMarkers);
	}

	/**
	 * Handles trails for the markers
	 *
	 * @param revertChanges
	 */
	public void handleTrails(boolean revertChanges) {
		HashSet<Trail> newTrails = new HashSet<>();
		for (int i = 0; i < boundingMarkers.size() - 1; i++) {
			Marker marker = boundingMarkers.get(i);
			Marker next = boundingMarkers.get(i + 1);

			Trail trail = new Trail(marker, next, this);
			newTrails.add(trail);
			if (trails.contains(trail)) {
				continue;
			}
			trails.add(trail);
			trail.start();
		}
		// add last trail
		if (boundingMarkers.size() >= 3) {
			Marker first = boundingMarkers.get(0);
			Marker last = boundingMarkers.get(boundingMarkers.size() - 1);
			Trail trail = new Trail(first, last, this);
			newTrails.add(trail);
			if (!trails.contains(trail)) {
				trails.add(trail);
				trail.start();
			}
		}

		trails.removeIf(trail -> {
			if (!newTrails.contains(trail)) {
				trail.stopTask(revertChanges);
				return true;
			}
			return false;
		});

	}

	private void removeMarker0(Marker marker, boolean revertChanges) {
		if (marker.hasPreviousData()) {
			Block block = marker.getLocation().getBlock();
			block.setBlockData(marker.getPreviousData(), true);

			if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
				block.getState().removeMetadata(ProtectionSettings.MARKER_KEY, BukkitMain.getInstance());
			}

			for (Player player : Bukkit.getOnlinePlayers()) {
				player.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getType());
			}
		}

		markers.remove(marker);
	}

	/**
	 * Handles removal of marker
	 *
	 * @param marker        the marker
	 * @param revertChanges if the marker gets removed permanently
	 */
	public void removeMarker(Marker marker, boolean revertChanges) {
		removeMarker0(marker, revertChanges);
		calculateBoundingMarkers(null);
		handleTrails(revertChanges);
		try {
			offerAccepting();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public ItemStack[] getStartingInventoryContent() {
		return startingInventoryContent;
	}

	// private Component getTransactionReceivedComponent(Transaction transaction) {
	// TextComponent.Builder transactionReceivedComponentBuilder = Component.text();

	// double amount = transaction.getTransactionAmount();
	// if (amount < 0) {
	// amount = Math.abs(amount);
	// }

	// transactionReceivedComponentBuilder.append(MessageManager.prefix());
	// transactionReceivedComponentBuilder.append(Component.text("[",
	// NamedTextColor.DARK_GRAY));
	// transactionReceivedComponentBuilder.append(
	// Component.text(transaction.getTransactionCurrency().getCurrencySymbol(),
	// NamedTextColor.YELLOW));
	// transactionReceivedComponentBuilder.append(Component.text("]",
	// NamedTextColor.DARK_GRAY));
	// transactionReceivedComponentBuilder.append(Component.space());

	// transactionReceivedComponentBuilder.append(Component.text("Du hast",
	// MessageManager.INFO));
	// transactionReceivedComponentBuilder.append(Component.space());
	// transactionReceivedComponentBuilder
	// .append(Component.text(amount, MessageManager.VARIABLE_VALUE));
	// transactionReceivedComponentBuilder.append(Component
	// .text(transaction.getTransactionCurrency().getCurrencySymbol(),
	// MessageManager.VARIABLE_VALUE));
	// transactionReceivedComponentBuilder.append(Component.space());

	// transactionReceivedComponentBuilder.append(Component.text("für dein
	// Grundstück bezahlt.", MessageManager.INFO));

	// if (transaction.getTransactionReceiver() instanceof User) {
	// User user = (User) transaction.getTransactionReceiver();
	// Player player = user.getLocalPlayer();

	// if (player.isOnline()) {
	// player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
	// }
	// }

	// return transactionReceivedComponentBuilder.build();
	// }
}
