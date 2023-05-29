package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.visualizer.color.ProtectionVisualizerColor;
import dev.slne.protect.bukkit.region.visual.visualizer.color.ProtectionVisualizerColor.VisualizerColor;

public abstract class ProtectionVisualizer<T extends ProtectedRegion> {

    private World world;
    private T region;
    private Player player;

    private List<Location> locations;
    private List<Location> oldLocations;

    private VisualizerColor color;
    private Map<Location, Integer> entityIds;

    /**
     * Create a new visualizer for a region.
     *
     * @param world  The world that the region is in.
     * @param region The region to visualize.
     * @param player The player that is visualizing the region.
     */
    protected ProtectionVisualizer(World world, T region, Player player) {
        this.world = world;
        this.region = region;
        this.player = player;

        this.locations = new ArrayList<>();
        this.oldLocations = new ArrayList<>();
        this.entityIds = new HashMap<>();
        this.applyProtectionColor();
    }

    /**
     * Visualize the region
     */
    public abstract List<Location> visualize();

    /**
     * Update the visualizer
     */
    public synchronized void update() {
        this.oldLocations = new ArrayList<>(this.locations);

        this.locations.clear();
        this.locations = this.visualize();
        this.locations = this.performDistanceCheck();

        List<Location> toRemoves = this.getToRemoveLocations();

        for (Location toRemove : toRemoves) {
            if (this.entityIds.containsKey(toRemove)) {
                this.killVisualizer(player, this.entityIds.get(toRemove));
                this.entityIds.remove(toRemove);
            }
        }

        this.visualizeLocations();
    }

    /**
     * Perform a distance check and return the locations which are in range
     *
     * @return the locations
     */
    private synchronized List<Location> performDistanceCheck() {
        List<Location> inDistance = new ArrayList<>();

        int entityViewDistance = this.calculateEntityDistanceWithViewDistance();

        for (Location location : this.locations) {
            if (location.distanceSquared(
                    this.getPlayer().getLocation()) <= entityViewDistance) {
                inDistance.add(location);
            }
        }

        return inDistance;
    }

    /**
     * Returns the locations which will be removed on the next update tick
     *
     * @return the locations
     */
    private synchronized List<Location> getToRemoveLocations() {
        List<Location> toRemove = new ArrayList<>();

        for (Location location : this.oldLocations) {
            if (!this.locations.contains(location)) {
                toRemove.add(location);
            }
        }

        return toRemove;
    }

    /**
     * Remove the visualizers
     *
     * @param player the player
     */
    public synchronized void remove() {
        for (Map.Entry<Location, Integer> entry : new ArrayList<>(this.entityIds.entrySet())) {
            this.killVisualizer(player, entry.getValue());
        }
    }

    /**
     * Kill one of the visualizers.
     *
     * @param entityId The entity id of the visualizer.
     */
    private synchronized void killVisualizer(Player player, int entityId) {
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyEntities);

        Location location = this.entityIds.keySet().stream().filter(loc -> this.entityIds.get(loc) == entityId)
                .findFirst().orElse(null);
        this.entityIds.remove(location);
    }

    /**
     * Get a random entity id.
     *
     * @return The entity id.
     */
    private synchronized int getRandomEntityId() {
        Random random = BukkitMain.getRandom();
        int entityId = random.nextInt(Integer.MAX_VALUE);

        while (flatMapAllUsedEntityIds().contains(entityId)) {
            entityId = random.nextInt(Integer.MAX_VALUE);
        }

        return entityId;
    }

    /**
     * Get all of the used entity ids.
     *
     * @return The entity ids.
     */
    private synchronized List<Integer> flatMapAllUsedEntityIds() {
        List<Integer> entityIdMap = new ArrayList<>();

        for (Map.Entry<Location, Integer> entityIdMapping : this.entityIds.entrySet()) {
            entityIdMap.add(entityIdMapping.getValue());
        }

        return entityIdMap;
    }

    /**
     * Visualize a list of locations.
     *
     * @param region    The region that the locations are in.
     * @param locations The locations to visualize.
     */
    private synchronized void visualizeLocations() {
        for (Location location : new ArrayList<>(getLocations())) {
            if (!region.contains(BlockVector3.at(location.getBlockX(), location.getBlockY(),
                    location.getBlockZ()))) {
                continue;
            }

            visualizeLocation(getPlayer(), location);
        }
    }

    /**
     * Visualize a location.
     *
     * @param player   the player
     * @param location The location to visualize.
     */
    private synchronized void visualizeLocation(Player player, Location location) {
        if (this.entityIds.containsKey(location)) {
            return;
        }

        int entityId = getRandomEntityId();
        UUID uuid = UUID.randomUUID();
        EntityType entityType = EntityTypes.BLOCK_DISPLAY;
        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
        float pitch = 0;
        float yaw = 0;
        float headYaw = 0;
        int data = 0;
        Vector3d velocity = new Vector3d(0, 0, 0);

        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(uuid),
                entityType, position, pitch, yaw, headYaw, data, Optional.of(velocity));

        List<EntityData> entityData = new ArrayList<>();
        entityData.add(new EntityData(22, EntityDataTypes.BLOCK_STATE, color.getId()));

        WrapperPlayServerEntityMetadata metaData = new WrapperPlayServerEntityMetadata(entityId, entityData);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntity);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metaData);

        this.entityIds.put(location, entityId);
    }

    /**
     * Applies a random color to the visualizer
     */
    private synchronized void applyRandomProtectionColor() {
        this.color = new ProtectionVisualizerColor().getRandomColor();
    }

    /**
     * Applies a protection color to the visualizer using the region owners
     */
    private synchronized void applyProtectionColor() {
        boolean ownsRegion = this.region.getOwners().contains(this.player.getUniqueId());

        if (ownsRegion) {
            this.color = VisualizerColor.OWNING;
        } else {
            this.color = VisualizerColor.NOT_OWNING;
        }
    }

    /**
     * Clamp a value between a minimum and maximum value.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    private synchronized int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Calculate the entity distance with the view distance.
     *
     * @return The entity distance.
     */
    private synchronized int calculateEntityDistanceWithViewDistance() {
        int clientViewDistance = getPlayer().getViewDistance();
        int serverViewDistance = Bukkit.getServer().getViewDistance();

        int viewDistance = Math.min(clientViewDistance, serverViewDistance);
        int maxViewDistance = ProtectionSettings.PROTECTION_VISUALIZER_MAX_DISTANCE;
        int minViewDistance = ProtectionSettings.PROTECTION_VISUALIZER_MIN_DISTANCE;

        viewDistance = viewDistance * 10;
        viewDistance = viewDistance * viewDistance;

        return clamp(viewDistance, minViewDistance, maxViewDistance);
    }

    /**
     * Get the world
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the region
     *
     * @return the region
     */
    public T getRegion() {
        return region;
    }

    /**
     * Get the locations
     *
     * @return the locations
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     * Get the color
     *
     * @return the color
     */
    public VisualizerColor getColor() {
        return color;
    }

    /**
     * Set the color
     *
     * @param color the color
     */
    public void setColor(VisualizerColor color) {
        this.color = color;
    }

    /**
     * Returns the {@link Player}
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the entity ids
     *
     * @return the entity ids
     */
    public Map<Location, Integer> getEntityIds() {
        return entityIds;
    }

    /**
     * Get the old locations
     *
     * @return the old locations
     */
    public List<Location> getOldLocations() {
        return oldLocations;
    }
}
