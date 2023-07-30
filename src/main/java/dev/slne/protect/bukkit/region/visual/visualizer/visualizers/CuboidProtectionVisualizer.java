package dev.slne.protect.bukkit.region.visual.visualizer.visualizers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;

public class CuboidProtectionVisualizer extends ProtectionVisualizer<ProtectedCuboidRegion> {

    /**
     * Create a new visualizer for a cuboid region.
     *
     * @param world  The world that the region is in.
     * @param region The cuboid region to visualize.
     * @param player The player that is visualizing the region.
     */
    public CuboidProtectionVisualizer(World world, ProtectedCuboidRegion region, Player player) {
        super(world, region, player);
    }

    @Override
    public List<Location> visualize() {

        BlockVector3 minimumPoint = getRegion().getMinimumPoint();
        BlockVector3 maximumPoint = getRegion().getMaximumPoint();

        Location minimumLocation = new Location(getWorld(), minimumPoint.getX(), minimumPoint.getY(),
                minimumPoint.getZ());
        Location maximumLocation = new Location(getWorld(), maximumPoint.getX(), maximumPoint.getY(),
                maximumPoint.getZ());

        List<Location> edgeLocations = new ArrayList<>();
        for (double x = minimumLocation.getX(); x <= maximumLocation.getX(); x++) {
            for (double y = minimumLocation.getY(); y <= maximumLocation.getY(); y++) {
                for (double z = minimumLocation.getZ(); z <= maximumLocation.getZ(); z++) {
                    if (x == minimumLocation.getX() || x == maximumLocation.getX()
                            || y == minimumLocation.getY() || y == maximumLocation.getY()
                            || z == minimumLocation.getZ() || z == maximumLocation.getZ()) {
                        edgeLocations.add(new Location(getWorld(), x, y, z).add(0.5, 0, 0.5));
                    }
                }
            }
        }

        return new ArrayList<>(edgeLocations);
    }

}
