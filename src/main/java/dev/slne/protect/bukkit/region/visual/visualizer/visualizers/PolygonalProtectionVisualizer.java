package dev.slne.protect.bukkit.region.visual.visualizer.visualizers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;

import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;

public class PolygonalProtectionVisualizer extends ProtectionVisualizer<ProtectedPolygonalRegion> {

    /**
     * Create a new visualizer for a region.
     *
     * @param world  The world that the region is in.
     * @param region The region to visualize.
     * @param player The player to visualize for.
     */
    public PolygonalProtectionVisualizer(World world, ProtectedPolygonalRegion region, Player player) {
        super(world, region, player);

        setScaleUp(true);
    }

    @Override
    public List<Location> visualize() {
        List<Location> locations = new ArrayList<>();
        List<BlockVector2> points = getRegion().getPoints();

        for (BlockVector2 point : points) {
            locations.addAll(this.formVisualizerPillar(point));
        }

        locations.addAll(this.formVisualizerLine(points));
        return locations;
    }

    /**
     * Visualize a pillar.
     *
     * @param point The point that the pillar is at.
     * @return The locations of the pillar.
     */
    private List<Location> formVisualizerPillar(BlockVector2 point) {
        List<Location> locations = new ArrayList<>();
        Block highestYBlock = getWorld().getHighestBlockAt(point.getBlockX(), point.getBlockZ(),
                HeightMap.MOTION_BLOCKING_NO_LEAVES);
        int highestY = highestYBlock.getY() + 1;
        locations.add(new Location(getWorld(), point.getBlockX(), highestY, point.getBlockZ()));

        return locations;
    }

    /**
     * Visualize a line.
     *
     * @param linePoints The points that the line is at.
     * @return The locations of the line.
     */
    private List<Location> formVisualizerLine(List<BlockVector2> linePoints) {
        List<Location> locations = new ArrayList<>();
        List<BlockVector2> addedLinePoints = new ArrayList<>(linePoints);

        for (int i = 0; i < linePoints.size(); i++) {
            BlockVector2 currentPoint = linePoints.get(i);
            BlockVector2 nextPoint = linePoints.get(i + 1 == linePoints.size() ? 0 : i + 1);

            List<BlockVector2> points = walkPointAToB(currentPoint, nextPoint);
            for (BlockVector2 point : points) {
                addedLinePoints.add(point);
                locations.add(new Location(getWorld(), point.getBlockX(), 0, point.getBlockZ()));
            }
        }

        List<Location> finalLocations = new ArrayList<>();
        for (Location location : locations) {
            BlockVector2 point = BlockVector2.at(location.getBlockX(), location.getBlockZ());

            int tries = 0;
            int maxTries = 4;

            while (tries < maxTries) {
                if (getRegion().contains(point)) {
                    break;
                }

                BlockVector2[] vectors = new BlockVector2[] {
                        point.add(1, 0),
                        point.add(-1, 0),
                        point.add(0, 1),
                        point.add(0, -1)
                };

                for (BlockVector2 usableVector : vectors) {
                    if (!addedLinePoints.contains(usableVector) && getRegion().contains(usableVector)) {
                        point = usableVector;
                    }
                }

                tries++;
            }

            if (point != null) {
                Block highestYBlock = getWorld().getHighestBlockAt(point.getBlockX(), point.getBlockZ(),
                        HeightMap.MOTION_BLOCKING_NO_LEAVES);
                int highestY = highestYBlock.getY() + 1;
                finalLocations.add(new Location(getWorld(), point.getBlockX(), highestY, point.getBlockZ()));
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

}
