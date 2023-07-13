package dev.slne.protect.bukkit.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.info.RegionCreationState;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.transaction.ProtectionBuyData;
import dev.slne.protect.bukkit.region.visual.Marker;
import dev.slne.protect.bukkit.region.visual.QuickHull;
import dev.slne.protect.bukkit.region.visual.Trail;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.transaction.core.currency.Currency;
import dev.slne.transaction.core.transaction.TransactionAddResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ProtectionRegion {

    private final Location startLocation;
    private final ProtectedRegion expandingProtection;
    private final ProtectionUser protectionUser;
    private final List<Marker> markers;
    private final HashSet<Trail> trails;
    private final ItemStack[] startingInventoryContent;
    private TemporaryProtectionRegion temporaryRegion;
    private List<Marker> boundingMarkers;

    public ProtectionRegion(ProtectionUser protectionUser, ProtectedRegion expandingProtection) {
        this.protectionUser = protectionUser;
        this.expandingProtection = expandingProtection;
        this.startLocation = protectionUser.getBukkitPlayer().getLocation();
        this.startingInventoryContent = protectionUser.getBukkitPlayer().getInventory().getContents().clone();

        this.boundingMarkers = new ArrayList<>();
        this.markers = new ArrayList<>();
        this.trails = new HashSet<>();
    }

    /**
     * Sets the old markers for the expanding region
     */
    public void setExpandingMarkers() {
        if (this.isExpandingRegion()) {
            World world = protectionUser.getBukkitPlayer().getWorld();
            List<BlockVector2> vector2s = expandingProtection.getPoints();

            for (BlockVector2 vector : vector2s) {
                Location location = new Location(world, vector.getX(), 0, vector.getZ());
                location.setY(world.getHighestBlockYAt(location, ProtectionSettings.PROTECTION_HEIGHTMAP) + (double) 1);

                createMarker(location.getBlock(), location.getBlock().getBlockData());
            }
        }
    }

    /**
     * Returns if the protection is expanding
     *
     * @return if the protection is expanding
     */
    public boolean isExpandingRegion() {
        return expandingProtection != null;
    }

    /**
     * Called when a new marker is created
     *
     * @param block        the block of the location
     * @param previousData the previous data in that location
     *
     * @return the new marker or null, if marker could not be created
     */
    public Marker createMarker(Block block, BlockData previousData) {
        Marker marker = new Marker(this, block.getLocation(), previousData);
        calculateBoundingMarkers(marker);

        if (! boundingMarkers.contains(marker)) {
            return null;
        }

        // Perform actual state operation
        RegionCreationState state = offerAccepting();

        if (state.equals(RegionCreationState.OVERLAPPING)) {
            this.removeMarker(marker);
            this.handleTrails();
            this.calculateBoundingMarkers(marker);

            return null;
        }

        markers.add(marker);
        handleTrails();

        return marker;
    }

    /**
     * Calculates the bounding markers for the given marker
     *
     * @param marker the marker
     */
    private void calculateBoundingMarkers(Marker marker) {
        ArrayList<Marker> newMarkers = new ArrayList<>(this.markers);

        if (marker != null) {
            newMarkers.add(marker);
        }

        boundingMarkers = new QuickHull().quickHull(newMarkers);
    }

    /**
     * Accepts the protection
     *
     * @return the {@link RegionCreationState}
     */
    protected RegionCreationState offerAccepting() {
        if (boundingMarkers.size() < ProtectionSettings.MIN_MARKERS) {
            protectionUser.sendMessage(MessageManager.getMoreMarkersComponent(boundingMarkers.size()));
            temporaryRegion = null;
            return RegionCreationState.MORE_MARKERS_NEEDED;
        }

        List<BlockVector2> vectors = new ArrayList<>();
        for (Marker marker : boundingMarkers) {
            vectors.add(BlockVector2.at(marker.getLocation().getX(), marker.getLocation().getZ()));
        }

        Player player = this.protectionUser.getBukkitPlayer();

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

            HashSet<String> owners = new HashSet<>();
            owners.add(protectionUser.getLocalPlayer().getUniqueId().toString());
            region.setFlag(Flags.NONPLAYER_PROTECTION_DOMAINS, owners);
        }

        this.temporaryRegion = new TemporaryProtectionRegion(
                player.getWorld(), region, manager);
        long area = temporaryRegion.getArea();

        if (this.isExpandingRegion()) {
            temporaryRegion.setEffectiveArea(area - ProtectionUtils.getArea(expandingProtection));
        } else {
            temporaryRegion.setEffectiveArea(area);
        }

        if (temporaryRegion.overlaps(expandingProtection)) {
            protectionUser.sendMessage(MessageManager.getOverlappingRegionsComponent());
            return RegionCreationState.OVERLAPPING;
        } else if (area <= ProtectionSettings.AREA_MIN_BLOCKS) {
            protectionUser.sendMessage(MessageManager.getAreaTooSmallComponent());
            return RegionCreationState.TOO_SMALL;
        } else if (area > ProtectionSettings.AREA_MAX_BLOCKS) {
            protectionUser.sendMessage(MessageManager.getAreaTooBigComponent());
            return RegionCreationState.TOO_LARGE;
        }

        Currency currency = Currency.currencyByName("CastCoin");

        if (currency == null) {
            protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
            return RegionCreationState.NO_CURRENCY;
        }

        double effectiveCost = this.calculateProtectionPrice(temporaryRegion);
        BigDecimal effectiveCostBigDecimal = BigDecimal.valueOf(effectiveCost);
        boolean hasEnoughCurrency = Boolean.TRUE.equals(protectionUser.hasEnoughCurrency(effectiveCostBigDecimal, currency).join());

        if (! hasEnoughCurrency) {
            MessageManager.sendAreaTooExpensiveComponent(protectionUser, area, effectiveCost);
        } else {
            MessageManager.sendAreaBuyableComponent(protectionUser, area, effectiveCost);
        }

        this.setTemporaryRegion(temporaryRegion);
        return RegionCreationState.SUCCESS;
    }

    /**
     * Handles removal of marker
     *
     * @param marker the marker
     */
    public void removeMarker(Marker marker) {
        removeMarker0(marker);
        calculateBoundingMarkers(null);
        handleTrails();

        offerAccepting();
    }

    /**
     * Handles trails for the markers
     */
    public void handleTrails() {
        HashSet<Trail> newTrails = new HashSet<>();

        for (int i = 0; i < boundingMarkers.size() - 1; i++) {
            Marker marker = boundingMarkers.get(i);
            Marker next = boundingMarkers.get(i + 1);

            Trail trail = new Trail(marker, next, this, true);
            newTrails.add(trail);

            if (! trails.contains(trail)) {
                trails.add(trail);
                trail.start();
            }
        }

        // Add last trail
        if (boundingMarkers.size() >= ProtectionSettings.MIN_MARKERS_LAST_CONNECTION) {
            Marker first = boundingMarkers.get(0);
            Marker last = boundingMarkers.get(boundingMarkers.size() - 1);

            Trail trail = new Trail(first, last, this, true);
            newTrails.add(trail);

            if (! trails.contains(trail)) {
                trails.add(trail);
                trail.start();
            }
        }

        trails.removeIf(trail -> {
            if (! newTrails.contains(trail)) {
                trail.stopTask();

                return true;
            }

            return false;
        });
    }

    /**
     * Calculates the price for the given region
     *
     * @param region the region
     *
     * @return the price
     */
    protected double calculateProtectionPrice(TemporaryProtectionRegion region) {
        return region.getEffectiveArea() * ProtectionSettings.PRICE_PER_BLOCK;
    }

    /**
     * Actually removes the marker
     *
     * @param marker the {@link Marker}
     */
    private void removeMarker0(Marker marker) {
        if (marker.hasPreviousData()) {
            Block block = marker.getLocation().getBlock();
            marker.restorePreviousData();

            if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
                block.getState().removeMetadata(ProtectionSettings.MARKER_KEY, BukkitMain.getInstance());
            }
        }

        markers.remove(marker);
    }

    /**
     * Finished the protection
     *
     * @return if successful
     */
    public boolean finishProtection() {
        if (temporaryRegion == null) {
            offerAccepting();
            return false;
        }

        if (temporaryRegion.overlapsUnownedRegion(protectionUser.getLocalPlayer())) {
            protectionUser.sendMessage(MessageManager.getOverlappingRegionsComponent());
            return false;
        }

        double effectiveCost = this.calculateProtectionPrice(temporaryRegion);
        BigDecimal effectiveCostBigDecimal = BigDecimal.valueOf(- effectiveCost);

        Currency currency = Currency.currencyByName("CastCoin");

        if (currency == null) {
            protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
            return false;
        }

        TransactionAddResult transactionResult = protectionUser
                .addTransaction(null, effectiveCostBigDecimal, currency, new ProtectionBuyData(this.startLocation != null ? this.startLocation.getWorld() : null, this.temporaryRegion.getRegion())).join();

        if (transactionResult != null && transactionResult.equals(TransactionAddResult.SUCCESS)) {
            this.temporaryRegion.protect();

            this.removeAllMarkers();
            protectionUser.resetRegionCreation();
            protectionUser.sendMessage(MessageManager.getProtectionCreatedComponent());
            return true;
        } else {
            protectionUser.sendMessage(MessageManager.getTooExpensiveToBuyComponent());
            return false;
        }
    }

    /**
     * Removes all markers
     */
    public void removeAllMarkers() {
        for (Marker marker : new ArrayList<>(markers)) {
            removeMarker0(marker);
        }

        calculateBoundingMarkers(null);
        handleTrails();
    }

    /**
     * Cancel the protection
     */
    public void cancelProtection() {
        this.removeAllMarkers();

        protectionUser.sendMessage(MessageManager.getProtectionCanceledComponent());
        protectionUser.resetRegionCreation();
    }

    /**
     * Returns the initial content of the inventory when the region creation is
     * entered
     *
     * @return the initial {@link ItemStack} array
     */
    public ItemStack[] getStartingInventoryContent() {
        return startingInventoryContent;
    }

    /**
     * Returns the marker count left
     *
     * @return the marker count left
     */
    public int getMarkerCountLeft() {
        return getMaxMarkerCount() - getCurrentMarkerCount();
    }

    /**
     * Returns the max marker count
     *
     * @return the max marker count
     */
    public int getMaxMarkerCount() {
        int expandingPoints = 0;

        if (expandingProtection != null) {
            expandingPoints = expandingProtection.getPoints().size();
        }

        return ProtectionSettings.MARKERS + expandingPoints;
    }

    /**
     * Returns the current marker count
     *
     * @return the current marker count
     */
    public int getCurrentMarkerCount() {
        return this.markers.size();
    }

    /**
     * Returns the expanding protection
     *
     * @return the expanding protection
     */
    public ProtectedRegion getExpandingProtection() {
        return expandingProtection;
    }

    /**
     * Returns the start location
     *
     * @return the {@link Location}
     */
    public Location getStartLocation() {
        return startLocation;
    }

    /**
     * Returns the {@link List} of bounding {@link Marker}s
     *
     * @return the {@link List} of bounding {@link Marker}s
     */
    public List<Marker> getBoundingMarkers() {
        return boundingMarkers;
    }

    /**
     * Returns the temporary region creation
     *
     * @return the temporary region creation
     */
    public TemporaryProtectionRegion getTemporaryRegion() {
        return temporaryRegion;
    }

    /**
     * Sets the temporary region creation
     *
     * @param temporaryRegion the temporary region creation
     */
    public void setTemporaryRegion(TemporaryProtectionRegion temporaryRegion) {
        this.temporaryRegion = temporaryRegion;
    }

    /**
     * Gets the {@link ProtectionUser} for this region
     *
     * @return the {@link ProtectionUser}
     */
    public ProtectionUser getProtectionUser() {
        return protectionUser;
    }
}
