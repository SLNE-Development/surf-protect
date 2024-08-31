package dev.slne.protect.bukkit.region;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.protection.flags.ProtectionFlagsMap;
import dev.slne.protect.bukkit.math.Mth;
import dev.slne.protect.bukkit.math.Mth.EffectiveCostResult;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.flags.ProtectionFlagsRegistry;
import dev.slne.protect.bukkit.region.info.RegionCreationState;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.transaction.ProtectionBuyData;
import dev.slne.protect.bukkit.region.visual.Marker;
import dev.slne.protect.bukkit.region.visual.QuickHull;
import dev.slne.protect.bukkit.region.visual.Trail;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProtectionRegion {

    private static final ComponentLogger LOGGER = ComponentLogger.logger("ProtectionRegion");

    private final Location startLocation;
    private final ProtectedRegion expandingProtection;
    private final ProtectionUser protectionUser;
    private final List<Marker> markers;
    private final HashSet<Trail> trails;
    private final ItemStack[] startingInventoryContent;
    private TemporaryProtectionRegion temporaryRegion;
    private List<Marker> boundingMarkers;

    private double worldBorderSize = ProtectionSettings.MAX_DISTANCE_FROM_PROTECTION_START;

    private boolean isProcessingTransaction = false;

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

                createMarker(location.getBlock(), location.getBlock().getBlockData(), true);
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
     * @param isExpanding  if the markers get placed because the plugin is expanding the region
     * @return the new marker or null, if marker could not be created
     */
    public Marker createMarker(Block block, BlockData previousData, boolean isExpanding) {
        Marker marker = new Marker(this, block.getLocation(), previousData);
        calculateBoundingMarkers(marker);

        if (!boundingMarkers.contains(marker)) {
            return null;
        }

        // Perform actual state operation
        if (!isExpanding) {
            RegionCreationState state = offerAccepting(false);

            if (state.equals(RegionCreationState.OVERLAPPING)) {
                this.removeMarker(marker);
                this.handleTrails();
                this.calculateBoundingMarkers(marker);

                return null;
            }
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
    protected RegionCreationState offerAccepting(boolean calculatePrice) {
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
            region =
                    new ProtectedPolygonalRegion(expandingProtection.getId(), vectors, ProtectionSettings.MIN_Y_WORLD,
                            ProtectionSettings.MAX_Y_WORLD);
            region.copyFrom(expandingProtection);
        } else {
            String name = player.getName() + "-" + RandomStringUtils.randomAlphabetic(5).toUpperCase();
            region =
                    new ProtectedPolygonalRegion(name, vectors, ProtectionSettings.MIN_Y_WORLD,
                            ProtectionSettings.MAX_Y_WORLD);
            region.getOwners().addPlayer(protectionUser.getLocalPlayer());

            for (ProtectionFlagsMap flagsMap : ProtectionFlagsMap.values()) {
                region.setFlag(flagsMap.getFlag(), flagsMap.getInitialState());
            }

            HashSet<String> owners = new HashSet<>();
            owners.add(protectionUser.getLocalPlayer().getUniqueId().toString());
            region.setFlag(Flags.NONPLAYER_PROTECTION_DOMAINS, owners);
        }

        // Get the center of the region and set the teleport location
        Region worldeditRegion = WorldEditRegionConverter.convertToRegion(region);
        Vector3 center = worldeditRegion.getCenter();
        Location teleportLocation = new Location(player.getWorld(), center.getX(), center.getY(), center.getZ());

        // Set TELE_LOC flag to the center of the region
        region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(teleportLocation));

        // Set SURF_PROTECT_FLAG if it does not exist already
        new RegionInfo(player.getWorld(), region);

        // Set SURF_PROTECTION flag to ALLOW
        region.setFlag(ProtectionFlagsRegistry.SURF_PROTECTION, StateFlag.State.ALLOW);

        this.temporaryRegion = new TemporaryProtectionRegion(player.getWorld(), region, manager);
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

        Optional<Currency> currency = TransactionApi.getCurrency("CastCoin");

        if (currency.isEmpty()) {
            protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
            return RegionCreationState.NO_CURRENCY;
        }

        final EffectiveCostResult effectiveCostResult = Mth.calculateEffectiveCost(teleportLocation, temporaryRegion);
        final double distanceToSpawn = teleportLocation.distance(teleportLocation.getWorld().getSpawnLocation());

        if (effectiveCostResult.effectiveCost() <= 0) {
            protectionUser.sendMessage(MessageManager.getAreaTooSmallComponent());
            return RegionCreationState.TOO_SMALL;
        }

        MessageManager.sendAreaBuyableComponent(protectionUser, area, effectiveCostResult.effectiveCost(), currency.get(), effectiveCostResult.pricePerBlock(), distanceToSpawn);

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

        offerAccepting(false);
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

            if (!trails.contains(trail)) {
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

            if (!trails.contains(trail)) {
                trails.add(trail);
                trail.start();
            }
        }

        trails.removeIf(trail -> {
            if (!newTrails.contains(trail)) {
                trail.stopTask();

                return true;
            }

            return false;
        });
    }

    /**
     * Calculates the price for the given region
     *
     * @param region        the region
     * @param pricePerBlock the price per block
     * @return the price
     */
    protected double calculateProtectionPrice(TemporaryProtectionRegion region, double pricePerBlock) {
        return region.getEffectiveArea() * pricePerBlock;
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
     */
    public void finishProtection() {
        if (temporaryRegion == null) {
            offerAccepting(true);
            return;
        }

        if (temporaryRegion.overlapsUnownedRegion(protectionUser.getLocalPlayer())) {
            protectionUser.sendMessage(MessageManager.getOverlappingRegionsComponent());
            return;
        }


        com.sk89q.worldedit.util.Location worldeditLocation = temporaryRegion.getRegion().getFlag(Flags.TELE_LOC);

        if (worldeditLocation == null) {
            protectionUser.sendMessage(MessageManager.getNoTeleportLocationComponent());
            return;
        }

        Location teleportLocation = BukkitAdapter.adapt(worldeditLocation);
        double pricePerBlock = ProtectionUtils.getProtectionPricePerBlock(teleportLocation);

        double effectiveCost = this.calculateProtectionPrice(temporaryRegion, pricePerBlock);
        BigDecimal effectiveCostBigDecimal = BigDecimal.valueOf(-effectiveCost);

        Optional<Currency> currency = TransactionApi.getCurrency("CastCoin");

        if (currency.isEmpty()) {
            protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
            return;
        }

        if (isProcessingTransaction) {
            protectionUser.sendMessage(MessageManager.getProtectionAlreadyProcessingComponent());
            return;
        }

        isProcessingTransaction = true;

        protectionUser.hasEnoughCurrency(BigDecimal.valueOf(effectiveCost), currency.get()).thenAcceptAsync(hasEnoughCurrency -> {
            if (!hasEnoughCurrency) {
                protectionUser.sendMessage(MessageManager.getTooExpensiveToBuyComponent());
                isProcessingTransaction = false;
            } else {
                protectionUser.addTransaction(
                        null,
                        effectiveCostBigDecimal,
                        currency.get(),
                        new ProtectionBuyData(this.startLocation != null ? this.startLocation.getWorld() : null, this.temporaryRegion.getRegion())
                ).thenAcceptAsync(transactionAddResult -> {
                    if (transactionAddResult != null && transactionAddResult.equals(TransactionAddResult.SUCCESS)) {
                        this.temporaryRegion.protect();

                        this.removeAllMarkers();
                        Bukkit.getScheduler().runTask(BukkitMain.getInstance(), protectionUser::resetRegionCreation);
                        protectionUser.sendMessage(MessageManager.getProtectionCreatedComponent());
                    } else {
                        protectionUser.sendMessage(MessageManager.getTooExpensiveToBuyComponent());
                    }

                    isProcessingTransaction = false;
                }).exceptionally(throwable -> {
                    LOGGER.error("Error while buying protection", throwable);
                    return null;
                });
            }

        }).exceptionally(throwable -> {
            LOGGER.error("Error while checking if user has enough currency", throwable);
            return null;
        });
    }

    /**
     * Removes all markers
     */
    public void removeAllMarkers() {
        Bukkit.getScheduler().runTask(BukkitMain.getInstance(), () -> {
            for (Marker marker : new ArrayList<>(markers)) {
                removeMarker0(marker);
            }

            calculateBoundingMarkers(null);
            handleTrails();
        });
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
     * Gets the {@link ProtectionUser} for this region
     *
     * @return the {@link ProtectionUser}
     */
    public ProtectionUser getProtectionUser() {
        return protectionUser;
    }

    /**
     * Returns the worldBorderSize
     *
     * @return the worldBorderSize
     */
    public double getWorldBorderSize() {
        return worldBorderSize;
    }

    /**
     * Sets the worldBorderSize
     *
     * @param worldBorderSize the worldBorderSize
     */
    public void setWorldBorderSize(double worldBorderSize) {
        this.worldBorderSize = worldBorderSize;
    }
}
