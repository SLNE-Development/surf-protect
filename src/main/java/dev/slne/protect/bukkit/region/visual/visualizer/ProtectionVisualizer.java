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
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerColor.VisualizerColor;
import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionVisualizer {

    private ProtectionUser protectionUser;

    private List<ProtectedRegion> protectedRegions;
    private Map<ProtectedRegion, Map<Location, Integer>> entityIds;
    private ProtectionVisualizerColor colorManager;

    /**
     * Create a new visualizer for a region.
     *
     * @param protectionUser The user that is visualizing the regions.
     */
    public ProtectionVisualizer(ProtectionUser protectionUser) {
        this.protectionUser = protectionUser;

        this.protectedRegions = new ArrayList<>();
        this.entityIds = new HashMap<>();
        this.colorManager = new ProtectionVisualizerColor();
    }

    /**
     * Visualize the regions.
     */
    public void visualizeRegions() {
        for (ProtectedRegion region : this.protectedRegions) {
            this.visualizeRegion(region);
        }
    }

    /**
     * Visualize a region.
     *
     * @param region The region to visualize.
     */
    public void visualizeRegion(ProtectedRegion region) {
        if (region instanceof ProtectedCuboidRegion cuboidRegion) {
            visualizeCuboid(cuboidRegion);
        } else if (region instanceof ProtectedPolygonalRegion polygonalRegion) {
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

        this.visualizeLocations(cuboidRegion, edgeLocations);
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
            locations.addAll(this.formVisualizerPillar(world, point));
        }

        locations.addAll(this.formVisualizerLine(polygonalRegion, world, points));

        this.visualizeLocations(polygonalRegion, locations);
    }

    /**
     * Visualize a pillar.
     *
     * @param world The world that the pillar is in.
     * @param point The point that the pillar is at.
     * @return The locations of the pillar.
     */
    private List<Location> formVisualizerPillar(World world, BlockVector2 point) {
        List<Location> locations = new ArrayList<>();
        Block highestYBlock = world.getHighestBlockAt(point.getBlockX(), point.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES);
        int highestY = highestYBlock.getY() + 1;
        int yHeightSettings = ProtectionSettings.PROTECTION_VISUALIZER_PILLAR_HEIGHT;

        int yHeight = highestY + yHeightSettings;

        for (double y = highestY; y <= yHeight; y++) {
            locations.add(new Location(world, point.getX(), y, point.getZ()));
        }

        return locations;
    }

    /**
     * Visualize a line.
     *
     * @param world      The world that the line is in.
     * @param linePoints The points that the line is at.
     * @return The locations of the line.
     */
    private List<Location> formVisualizerLine(ProtectedRegion region, World world, List<BlockVector2> linePoints) {
        List<Location> locations = new ArrayList<>();
        List<BlockVector2> addedLinePoints = new ArrayList<>(linePoints);

        for (int i = 0; i < linePoints.size(); i++) {
            BlockVector2 currentPoint = linePoints.get(i);
            BlockVector2 nextPoint = linePoints.get(i + 1 == linePoints.size() ? 0 : i + 1);

            List<BlockVector2> points = walkPointAToB(currentPoint, nextPoint);
            for (BlockVector2 point : points) {
                addedLinePoints.add(point);
                locations.add(new Location(world, point.getBlockX(), 0, point.getBlockZ()));
            }
        }

        List<Location> finalLocations = new ArrayList<>();
        for (Location location : locations) {
            BlockVector2 point = BlockVector2.at(location.getBlockX(), location.getBlockZ());

            int tries = 0;
            int maxTries = 4;

            while (tries < maxTries) {
                if (region.contains(point)) {
                    break;
                }

                BlockVector2[] vectors = new BlockVector2[] {
                        point.add(1, 0),
                        point.add(-1, 0),
                        point.add(0, 1),
                        point.add(0, -1)
                };

                for (BlockVector2 usableVector : vectors) {
                    if (!addedLinePoints.contains(usableVector) && region.contains(usableVector)) {
                        point = usableVector;
                    }
                }

                tries++;
            }

            if (point != null) {
                Block highestYBlock = world.getHighestBlockAt(point.getBlockX(), point.getBlockZ(),
                        HeightMap.MOTION_BLOCKING_NO_LEAVES);
                int highestY = highestYBlock.getY() + 1;
                int yHeightSettings = ProtectionSettings.PROTECTION_VISUALIZER_WALKER_HEIGHT;

                int yHeight = highestY + yHeightSettings;

                for (double y = highestY; y <= yHeight; y++) {
                    finalLocations.add(new Location(world, point.getBlockX(), y, point.getBlockZ()));
                }
            }
        }

        return finalLocations;
    }

    /**
     * Walk from point A to point B.
     *
     * @param pointStart The starting point.
     * @param pointEnd   The ending point.
     * @return The points in between.
     */
    private List<BlockVector2> walkPointAToB(BlockVector2 pointStart, BlockVector2 pointEnd) {
        List<BlockVector2> points = new ArrayList<>();

        int x1 = pointStart.getBlockX();
        int z1 = pointStart.getBlockZ();
        int x2 = pointEnd.getBlockX();
        int z2 = pointEnd.getBlockZ();

        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        int err = dx - dz;

        while (true) {
            points.add(BlockVector2.at(x1, z1));

            if (x1 == x2 && z1 == z2) {
                break;
            }

            int e2 = 2 * err;

            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }

            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }
        }

        return points;
    }

    /**
     * Visualize a list of locations.
     *
     * @param region    The region that the locations are in.
     * @param locations The locations to visualize.
     */
    private void visualizeLocations(ProtectedRegion region, List<Location> locations) {
        VisualizerColor color = this.getColorManager().getRandomColor();

        for (Location location : locations) {
            if (!region.contains(BlockVector3.at(location.getBlockX(), location.getBlockY(),
                    location.getBlockZ()))) {
                continue;
            }

            visualizeLocation(region, location, color);
        }
    }

    /**
     * Visualize a location.
     *
     * @param region   The region that the location is in.
     * @param location The location to visualize.
     */
    private void visualizeLocation(ProtectedRegion region, Location location, VisualizerColor color) {
        Player player = this.protectionUser.getBukkitPlayer();

        if (!this.entityIds.containsKey(region)) {
            this.entityIds.put(region, new HashMap<>());
        }

        Map<Location, Integer> entityIdMap = this.entityIds.get(region);

        if (entityIdMap.containsKey(location)) {
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

        entityIdMap.put(location, entityId);
    }

    /**
     * Kill all of the visualizers for the current protection.
     */
    private void killVisualizers() {
        for (Map<Location, Integer> entityIdMap : new ArrayList<>(this.entityIds.values())) {
            for (int entityId : new ArrayList<>(entityIdMap.values())) {
                this.killVisualizer(entityId);
            }
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

        for (Map<Location, Integer> entityIdMap : this.entityIds.values()) {
            entityIdMap.values().remove(entityId);
        }

        this.entityIds.values().removeIf(Map::isEmpty);
        this.entityIds.values().removeIf(entityIdMap -> entityIdMap.values().isEmpty());
    }

    /**
     * Get a random entity id.
     *
     * @return The entity id.
     */
    private int getRandomEntityId() {
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

        for (Map<Location, Integer> entityIdMapping : this.entityIds.values()) {
            entityIdMap.addAll(entityIdMapping.values());
        }

        return entityIdMap;
    }

    /**
     * Returns the list of protected regions that are being visualized
     *
     * @return The list of protected regions
     */
    public List<ProtectedRegion> getProtectedRegions() {
        return protectedRegions;
    }

    /**
     * Returns the color manager
     *
     * @return The color manager
     */
    public ProtectionVisualizerColor getColorManager() {
        return colorManager;
    }

    /**
     * Returns the entity ids
     *
     * @return The entity ids
     */
    public Map<ProtectedRegion, Map<Location, Integer>> getEntityIds() {
        return entityIds;
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
