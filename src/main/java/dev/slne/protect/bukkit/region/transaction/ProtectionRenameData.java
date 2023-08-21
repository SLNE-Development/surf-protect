package dev.slne.protect.bukkit.region.transaction;

import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.transaction.api.transaction.data.TransactionData;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ProtectionRenameData implements TransactionData {

    private World world;
    private ProtectedRegion region;

    private String fromName;
    private String toName;

    /**
     * Construct a new {@link ProtectionRenameData}
     *
     * @param world    The world
     * @param region   The region
     * @param fromName The old name
     * @param toName   The new name
     */
    public ProtectionRenameData(World world, ProtectedRegion region, String fromName, String toName) {
        this.region = region;
        this.world = world;
    }

    @Override
    public String toJson() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("type", "rename");
        jsonObject.addProperty("region_id", region.getId());
        jsonObject.addProperty("from_name", fromName);
        jsonObject.addProperty("to_name", toName);
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

        fromName = jsonObject.get("from_name").getAsString();
        toName = jsonObject.get("to_name").getAsString();
    }

    /**
     * Returns the region
     *
     * @return The region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * Returns the old name
     *
     * @return The old name
     */
    public String getFromName() {
        return fromName;
    }

    /**
     * Returns the new name
     *
     * @return The new name
     */
    public String getToName() {
        return toName;
    }

    /**
     * Returns the world
     *
     * @return The world
     */
    public World getWorld() {
        return world;
    }
}
