package dev.slne.surf.protect.paper.region.visual.visualizer.visualizers;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.surf.protect.paper.math.Mth;
import dev.slne.surf.protect.paper.region.visual.visualizer.ProtectionVisualizer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.List;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PolygonalProtectionVisualizer extends ProtectionVisualizer<ProtectedRegion> {

  /**
   * Create a new visualizer for a region.
   *
   * @param world  The world that the region is in.
   * @param region The region to visualize.
   * @param player The player to visualize for.
   */
  public PolygonalProtectionVisualizer(World world, ProtectedRegion region, Player player) {
    super(world, region, player);

    setScaleUp(true);
  }

  @Override
  public ObjectList<Location> visualize() {
    final ObjectList<Location> locations = new ObjectArrayList<>();
    final List<BlockVector2> points = getRegion().getPoints();

    for (final BlockVector2 point : points) {
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
  private ObjectList<Location> formVisualizerPillar(BlockVector2 point) {
    final ObjectList<Location> locations = new ObjectArrayList<>(1);

    addHighestBlockIfLoaded(locations, point);

    return locations;
  }

  /**
   * Visualize a line.
   *
   * @param linePoints The points that the line is at.
   * @return The locations of the line.
   */
  private ObjectList<Location> formVisualizerLine(List<BlockVector2> linePoints) {
    final ObjectList<Location> locations = new ObjectArrayList<>();
    final ObjectList<BlockVector2> addedLinePoints = new ObjectArrayList<>(linePoints);

    for (int i = 0; i < linePoints.size(); i++) {
      final BlockVector2 currentPoint = linePoints.get(i);
      final BlockVector2 nextPoint = linePoints.get(i + 1 == linePoints.size() ? 0 : i + 1);

      for (final BlockVector2 point : walkPointAToB(currentPoint, nextPoint)) {
        addedLinePoints.add(point);
        locations.add(new Location(getWorld(), point.x(), 0, point.z()));
      }
    }

    final ObjectList<Location> finalLocations = new ObjectArrayList<>(locations.size());
    for (final Location location : locations) {
      BlockVector2 point = BlockVector2.at(location.blockX(), location.blockZ());

      int tries = 0;
      final int maxTries = 4;

      while (tries < maxTries) {
        if (getRegion().contains(point)) {
          break;
        }

        final BlockVector2[] vectors = new BlockVector2[]{
            point.add(1, 0),
            point.add(-1, 0),
            point.add(0, 1),
            point.add(0, -1)
        };

        for (final BlockVector2 usableVector : vectors) {
          if (!addedLinePoints.contains(usableVector) && getRegion().contains(usableVector)) {
            point = usableVector;
          }
        }

        tries++;
      }

      if (point != null) {
        addHighestBlockIfLoaded(finalLocations, point);
      }
    }

    return finalLocations;
  }

  private void addHighestBlockIfLoaded(List<Location> finalLocations, BlockVector2 point) {
    final World world = getWorld();
    final int pointX = point.x();
    final int pointZ = point.z();

    if (world.isChunkLoaded(pointX >> 4, pointZ >> 4)) {
      final Block highestYBlock = world.getHighestBlockAt(pointX, pointZ,
          HeightMap.MOTION_BLOCKING_NO_LEAVES);
      final int highestY = highestYBlock.getY() + 1;
      finalLocations.add(new Location(world, pointX, highestY, pointZ));
    }
  }

  /**
   * Walk from point A to point B.
   *
   * @param pointStart The starting point.
   * @param pointEnd   The ending point.
   * @return The points in between.
   */
  private ObjectList<BlockVector2> walkPointAToB(BlockVector2 pointStart, BlockVector2 pointEnd) {
    final ObjectList<BlockVector2> points = new ObjectArrayList<>();

    Mth.walkCoordinatesAToB(pointStart.x(), pointStart.z(), pointEnd.x(),
        pointEnd.z(),
        (x, z) -> points.add(BlockVector2.at(x, z)));

    return points;
  }

}
