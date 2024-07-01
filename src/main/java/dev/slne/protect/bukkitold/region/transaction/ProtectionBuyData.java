package dev.slne.protect.bukkitold.region.transaction;

import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.transaction.api.transaction.data.TransactionData;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ProtectionBuyData implements TransactionData {

  private World world;
  private ProtectedRegion region;

  /**
   * Construct a new {@link ProtectionBuyData}
   *
   * @param world  The world
   * @param region The region
   */
  public ProtectionBuyData(World world, ProtectedRegion region) {
    this.region = region;
    this.world = world;
  }


  @Override
  public String toJson() {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("type", "buy");
    jsonObject.addProperty("region_id", region.getId());
    jsonObject.addProperty("world_name", world.getName());

    return jsonObject.toString();
  }

  @Override
  public void fromJson(String json) {
    JsonObject jsonObject = new JsonObject();

    this.world = Bukkit.getWorld(jsonObject.get("world_name").getAsString());
    this.region =
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world))
            .getRegion(jsonObject.get("region_id").getAsString());
  }

  /**
   * Returns the region
   *
   * @return The region
   */
  public ProtectedRegion getRegion() {
    return region;
  }

}
