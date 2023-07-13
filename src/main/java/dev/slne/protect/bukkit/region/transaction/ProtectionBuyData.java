package dev.slne.protect.bukkit.region.transaction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.data.core.gson.GsonConverter;
import dev.slne.transaction.core.transaction.TransactionData;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class ProtectionBuyData extends TransactionData {

    private final World world;
    private final ProtectedRegion region;

    /**
     * Construct a new {@link ProtectionBuyData}
     *
     * @param region The region
     */
    public ProtectionBuyData(World world, ProtectedRegion region) {
        this.region = region;
        this.world = world;
    }

    @Override
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("region_id", region.getId());
        jsonObject.addProperty("world_name", world.getName());

        return jsonObject.toString();
    }

    @Override
    public void fromJson(String json) {
        JsonObject jsonObject = new GsonConverter().fromJson(json, JsonObject.class);
        JsonElement regionIdElement = jsonObject.get("region_id");
        JsonElement worldNameElement = jsonObject.get("world_name");

        if (regionIdElement == null || regionIdElement.isJsonNull() || worldNameElement == null
                || worldNameElement.isJsonNull()) {
            return;
        }

        String worldName = worldNameElement.getAsString();
        String regionId = regionIdElement.getAsString();

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            return;
        }

        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(world);
        WorldGuard.getInstance().getPlatform().getRegionContainer().get(worldEditWorld).getRegion(regionId);
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
