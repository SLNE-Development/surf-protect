package dev.slne.protect.paper.region.visual.visualizer;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.paper.region.settings.ProtectionSettings;
import dev.slne.protect.paper.region.visual.visualizer.color.VisualizerColor;
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.SurfBukkitNmsCommonBridge;
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.packets.PacketOperation;
import dev.slne.surf.surfapi.bukkit.api.nms.bridges.packets.entity.SurfBukkitNmsSpawnPacketsKt;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import kotlin.Unit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3f;

public abstract class ProtectionVisualizer<T extends ProtectedRegion> implements Closeable {

  private final World world;
  private final T region;
  private final Player player;
  private final Object2IntMap<Location> displays;
  private volatile ObjectList<Location> locations;
  private volatile ObjectList<Location> oldLocations;
  private final VisualizerColor color;
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

    this.locations = ObjectLists.synchronize(new ObjectArrayList<>());
    this.oldLocations = ObjectLists.synchronize(new ObjectArrayList<>());
    this.displays = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());
    this.color = determineProtectionColor();

    this.scaleUp = false;
  }

  /**
   * Determines a protection color to the visualizer using the region owners
   */
  private VisualizerColor determineProtectionColor() {
    final UUID playerUniqueId = this.player.getUniqueId();
    final boolean ownsRegion = this.region.getOwners().contains(playerUniqueId);
    final boolean memberRegion = this.region.getMembers().contains(playerUniqueId);

    return ownsRegion ? VisualizerColor.OWNING
        : memberRegion ? VisualizerColor.MEMBER : VisualizerColor.NOT_OWNING;
  }

  /**
   * Update the visualizer
   */
  public void update() {
    this.oldLocations = ObjectLists.synchronize(new ObjectArrayList<>(locations));

    this.locations.clear();
    this.locations = ObjectLists.synchronize(this.performDistanceCheck(this.visualize()));

    final IntList toRemoveEntityIds = getToRemoveEntityIds();

    SurfBukkitNmsSpawnPacketsKt.getNmsSpawnPackets().despawn(toRemoveEntityIds);
    toRemoveEntityIds.forEach(displays::removeInt);

    this.visualizeLocations();
  }

  /**
   * Visualize the region
   */
  public abstract ObjectList<Location> visualize();

  /**
   * Perform a distance check and return the locations which are in range
   *
   * @return the locations
   */
  private ObjectList<Location> performDistanceCheck(@NotNull ObjectList<Location> toCheck) {
    final int entityViewDistance = this.calculateEntityDistanceWithViewDistance();
    final World playerWorld = getPlayer().getWorld();
    final Location playerLocation = getPlayer().getLocation();

    return toCheck.stream()
        .filter(location -> location.getWorld().equals(playerWorld))
        .filter(location -> entityViewDistance >= location.distanceSquared(playerLocation))
        .collect(ObjectArrayList::new, ObjectList::add, ObjectList::addAll);
  }

  /**
   * Returns the locations which will be removed on the next update tick
   *
   * @return the locations
   */
  private IntList getToRemoveEntityIds() {
    return oldLocations.stream()
        .filter(location -> !locations.contains(location))
        .mapToInt(displays::getInt)
        .collect(IntArrayList::new, IntList::add, IntList::addAll);
  }

  /**
   * Visualize a list of locations.
   */
  private void visualizeLocations() {
    getLocations().stream()
        .filter(loc -> region.contains(loc.blockX(), loc.blockY(), loc.blockZ()))
        .map(this::visualizeLocation)
        .filter(Objects::nonNull)
        .reduce(PacketOperation::add)
        .ifPresent(operation -> operation.execute(getPlayer()));
  }

  /**
   * Calculate the entity distance with the view distance.
   *
   * @return The entity distance.
   */
  private int calculateEntityDistanceWithViewDistance() {
    final int clientViewDistance = getPlayer().getViewDistance();
    final int serverViewDistance = Bukkit.getServer().getViewDistance();

    int viewDistance = Math.min(clientViewDistance, serverViewDistance);
    viewDistance *= 10;
    viewDistance *= viewDistance;

    return GenericMath.clamp(
        viewDistance,
        ProtectionSettings.PROTECTION_VISUALIZER_MIN_DISTANCE,
        ProtectionSettings.PROTECTION_VISUALIZER_MAX_DISTANCE
    );
  }

  /**
   * Returns the {@link Player}
   *
   * @return the player
   */
  public final Player getPlayer() {
    return player;
  }

  /**
   * Get the locations
   *
   * @return the locations
   */
  public final ObjectList<Location> getLocations() {
    return locations;
  }

  /**
   * Visualize a location.
   *
   * @param location The location to visualize.
   */
  private @Nullable PacketOperation visualizeLocation(Location location) {
    if (this.displays.containsKey(location)) {
      return null;
    }

    final int entityId = SurfBukkitNmsCommonBridge.Companion.getNextEntityId();
    final PacketOperation operation = SurfBukkitNmsSpawnPacketsKt.getNmsSpawnPackets().spawnBlockDisplay(
        entityId,
        location,
        settings -> {

          if (this.scaleUp) {
            settings.setScale(new Vector3f(1, ProtectionSettings.PROTECTION_VISUALIZER_HEIGHT, 1));
          }

          settings.setBlockData(color.getBlockData());
          return Unit.INSTANCE;
        }
    );

    this.displays.put(location, entityId);

    return operation;
  }

  @Override
  public void close() throws IOException {
    displays.values().intStream()
        .mapToObj(SurfBukkitNmsSpawnPacketsKt.getNmsSpawnPackets()::despawn)
        .reduce(PacketOperation::add)
        .ifPresent(packetOperation -> packetOperation.execute(getPlayer()));

    displays.clear();
  }

  /**
   * Get the world
   *
   * @return the world
   */
  public final World getWorld() {
    return world;
  }

  /**
   * Get the region
   *
   * @return the region
   */
  public final T getRegion() {
    return region;
  }

  /**
   * Sets if the visualizer should scale up
   *
   * @param scaleUp if the visualizer should scale up
   */
  public final void setScaleUp(boolean scaleUp) {
    this.scaleUp = scaleUp;
  }
}
