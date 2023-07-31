package dev.slne.protect.bukkit.region.visual.visualizer;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.visualizer.color.ProtectionVisualizerColor.VisualizerColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public abstract class ProtectionVisualizer<T extends ProtectedRegion> {

    private final World world;
    private final T region;
    private final Player player;
    private final Map<Location, Integer> entityIds;
    private List<Location> locations;
    private List<Location> oldLocations;
    private VisualizerColor color;
    private boolean scaleUp;

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

        this.scaleUp = false;
    }

    /**
     * Applies a protection color to the visualizer using the region owners
     */
    private void applyProtectionColor() {
        boolean ownsRegion = this.region.getOwners().contains(this.player.getUniqueId());

        if (ownsRegion) {
            this.color = VisualizerColor.OWNING;
        } else {
            this.color = VisualizerColor.NOT_OWNING;
        }
    }

    /**
     * Update the visualizer
     */
    public void update() {
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
     * Visualize the region
     */
    public abstract List<Location> visualize();

    /**
     * Perform a distance check and return the locations which are in range
     *
     * @return the locations
     */
    private List<Location> performDistanceCheck() {
        List<Location> inDistance = new ArrayList<>();

        int entityViewDistance = this.calculateEntityDistanceWithViewDistance();

        for (Location location : new ArrayList<>(this.locations)) {
            if (!location.getWorld().equals(this.getPlayer().getLocation().getWorld())) {
                continue;
            }

            if (entityViewDistance >= location.distanceSquared(this.getPlayer().getLocation())) {
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
    private List<Location> getToRemoveLocations() {
        List<Location> toRemove = new ArrayList<>();

        for (Location location : this.oldLocations) {
            if (!this.locations.contains(location)) {
                toRemove.add(location);
            }
        }

        return toRemove;
    }

    /**
     * Kill one of the visualizers.
     *
     * @param entityId The entity id of the visualizer.
     */
    private void killVisualizer(Player player, int entityId) {
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyEntities);

        Location location = this.entityIds.keySet().stream().filter(loc -> this.entityIds.get(loc) == entityId)
                .findFirst().orElse(null);
        this.entityIds.remove(location);
    }

    /**
     * Visualize a list of locations.
     */
    private void visualizeLocations() {
        for (Location location : new ArrayList<>(getLocations())) {
            if (!region.contains(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))) {
                continue;
            }

            visualizeLocation(getPlayer(), location);
        }
    }

    /**
     * Calculate the entity distance with the view distance.
     *
     * @return The entity distance.
     */
    private int calculateEntityDistanceWithViewDistance() {
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
     * Returns the {@link Player}
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
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
     * Visualize a location.
     *
     * @param player   the player
     * @param location The location to visualize.
     */
    private void visualizeLocation(Player player, Location location) {
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

        if (this.scaleUp) {
            Vector3f scale = new Vector3f(1, ProtectionSettings.PROTECTION_VISUALIZER_HEIGHT, 1);
            entityData.add(new EntityData(11, EntityDataTypes.VECTOR3F, scale));
        }

        WrapperPlayServerEntityMetadata metaData = new WrapperPlayServerEntityMetadata(entityId, entityData);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntity);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metaData);

        this.entityIds.put(location, entityId);
    }

    /**
     * Clamp a value between a minimum and maximum value.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     *
     * @return The clamped value.
     */
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Get a random entity id.
     *
     * @return The entity id.
     */
    private int getRandomEntityId() {
        Random random = BukkitMain.getRandom();
        int entityId = random.nextInt(Integer.MAX_VALUE);
        Collection<Integer> usedEntityIds = new ArrayList<>(this.entityIds.values());

        while (usedEntityIds.contains(entityId)) {
            entityId = random.nextInt(Integer.MAX_VALUE);
        }

        return entityId;
    }

    /**
     * Remove the visualizers
     */
    public void remove() {
        for (Map.Entry<Location, Integer> entry : new ArrayList<>(this.entityIds.entrySet())) {
            this.killVisualizer(player, entry.getValue());
        }
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
     * Sets if the visualizer should scale up
     *
     * @param scaleUp if the visualizer should scale up
     */
    public void setScaleUp(boolean scaleUp) {
        this.scaleUp = scaleUp;
    }
}
