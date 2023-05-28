package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

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
import dev.slne.protect.bukkit.region.visual.visualizer.color.ProtectionVisualizerColor;
import dev.slne.protect.bukkit.region.visual.visualizer.color.ProtectionVisualizerColor.VisualizerColor;

public abstract class ProtectionVisualizer<T extends ProtectedRegion> {

    private World world;
    private T region;
    private Player player;

    private List<Location> locations;
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
        this.entityIds = new HashMap<>();
        this.applyRandomProtectionColor();
    }

    /**
     * Visualize the region
     */
    public abstract void visualize();

    /**
     * Remove the visualizers
     *
     * @param player the player
     */
    public void remove() {
        for (Map.Entry<Location, Integer> entry : this.entityIds.entrySet()) {
            this.killVisualizer(player, entry.getValue());
        }
    }

    /**
     * Kill one of the visualizers.
     *
     * @param entityId The entity id of the visualizer.
     */
    protected void killVisualizer(Player player, int entityId) {
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
    protected int getRandomEntityId() {
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
    private List<Integer> flatMapAllUsedEntityIds() {
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
    protected void visualizeLocations() {
        for (Location location : getLocations()) {
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
    protected void visualizeLocation(Player player, Location location) {
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
    protected void applyRandomProtectionColor() {
        this.color = new ProtectionVisualizerColor().getRandomColor();
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
}
