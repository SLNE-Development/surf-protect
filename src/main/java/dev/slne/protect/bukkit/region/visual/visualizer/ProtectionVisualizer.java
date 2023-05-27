package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionVisualizer {

    private ProtectedRegion protectedRegion;
    private ProtectionUser protectionUser;

    private Map<Location, Integer> entityIds;

    /**
     * Create a new visualizer for a region.
     *
     * @param region The region to visualize.
     */
    public ProtectionVisualizer(ProtectedRegion region, ProtectionUser protectionUser) {
        this.protectedRegion = region;
        this.protectionUser = protectionUser;

        this.entityIds = new HashMap<>();
    }

    /**
     * Visualize the region.
     */
    public void visualize() {
        if (this.protectedRegion instanceof ProtectedCuboidRegion cuboidRegion) {
            visualizeCuboid(cuboidRegion);
        } else if (this.protectedRegion instanceof ProtectedPolygonalRegion polygonalRegion) {
            visualizePolygonal(polygonalRegion);
        } else {
            throw new IllegalArgumentException("Region type not supported.");
        }
    }

    /**
     * Stop visualizing the region.
     */
    public void stopVisualizing() {
        this.killVisualizers();
    }

    /**
     * Visualize a cuboid region.
     *
     * @param cuboidRegion The region to visualize.
     */
    private void visualizeCuboid(ProtectedCuboidRegion cuboidRegion) {
        Player player = this.protectionUser.getBukkitPlayer();
        World world = player.getWorld();

        BlockVector3 minimumPoint = cuboidRegion.getMinimumPoint();
        BlockVector3 maximumPoint = cuboidRegion.getMaximumPoint();

        Location minimumLocation = new Location(world, minimumPoint.getX(), minimumPoint.getY(), minimumPoint.getZ());
        Location maximumLocation = new Location(world, maximumPoint.getX(), maximumPoint.getY(), maximumPoint.getZ());

        List<Location> edgeLocations = new ArrayList<>();
        for (double x = minimumLocation.getX(); x <= maximumLocation.getX(); x++) {
            for (double y = minimumLocation.getY(); y <= maximumLocation.getY(); y++) {
                for (double z = minimumLocation.getZ(); z <= maximumLocation.getZ(); z++) {
                    if (x == minimumLocation.getX() || x == maximumLocation.getX()
                            || y == minimumLocation.getY() || y == maximumLocation.getY()
                            || z == minimumLocation.getZ() || z == maximumLocation.getZ()) {
                        edgeLocations.add(new Location(world, x, y, z).add(0.5, 0, 0.5));
                    }
                }
            }
        }

        this.visualizeLocations(edgeLocations);
    }

    /**
     * Visualize a polygonal region.
     *
     * @param polygonalRegion The region to visualize.
     */
    private void visualizePolygonal(ProtectedPolygonalRegion polygonalRegion) {
        Player player = this.protectionUser.getBukkitPlayer();
        List<BlockVector2> points = polygonalRegion.getPoints();
        List<Location> locations = new ArrayList<>();
        World world = player.getWorld();

        for (BlockVector2 point : points) {

            Block highestYBlock = world.getHighestBlockAt(point.getBlockX(), point.getBlockZ(),
                    HeightMap.MOTION_BLOCKING_NO_LEAVES);
            int highestY = highestYBlock.getY();
            int yHeightSettings = ProtectionSettings.PROTECTION_VISUALIZER_HEIGHT;

            int yHeight = highestY + yHeightSettings;

            for (double y = highestY; y <= yHeight; y++) {
                locations.add(new Location(player.getWorld(), point.getX(), y, point.getZ()).add(0.5, 0, 0.5));
            }
        }

        this.visualizeLocations(locations);
    }

    /**
     * Visualize a list of locations.
     *
     * @param locations The locations to visualize.
     */
    private void visualizeLocations(List<Location> locations) {
        for (Location location : locations) {
            if (!location.getBlock().getType().isAir()) {
                continue;
            }

            visualizeLocation(location);
        }
    }

    /**
     * Visualize a location.
     *
     * @param location The location to visualize.
     */
    private void visualizeLocation(Location location) {
        Player player = this.protectionUser.getBukkitPlayer();

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
        entityData.add(new EntityData(22, EntityDataTypes.BLOCK_STATE, 5947));

        // RED 5956

        WrapperPlayServerEntityMetadata metaData = new WrapperPlayServerEntityMetadata(entityId, entityData);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntity);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metaData);

        this.entityIds.put(location, entityId);
    }

    /**
     * Kill all of the visualizers for the current protection.
     */
    private void killVisualizers() {
        for (int entityId : new ArrayList<>(this.entityIds.values())) {
            this.killVisualizer(entityId);
        }
    }

    /**
     * Kill one of the visualizers.
     *
     * @param entityId The entity id of the visualizer.
     */
    private void killVisualizer(int entityId) {
        Player player = this.protectionUser.getBukkitPlayer();
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityId);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyEntities);
    }

    /**
     * Get a random entity id.
     *
     * @return The entity id.
     */
    private int getRandomEntityId() {
        Random random = BukkitMain.getRandom();
        int entityId = random.nextInt(Integer.MAX_VALUE);

        while (this.entityIds.containsValue(entityId)) {
            entityId = random.nextInt(Integer.MAX_VALUE);
        }

        return entityId;
    }

    /**
     * Get the region that is being visualized.
     *
     * @return The region.
     */
    public ProtectedRegion getProtectedRegion() {
        return protectedRegion;
    }

    /**
     * Get the user that is visualizing the region.
     *
     * @return The user.
     */
    public ProtectionUser getProtectionUser() {
        return protectionUser;
    }
}
