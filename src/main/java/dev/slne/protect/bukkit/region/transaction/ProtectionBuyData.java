package dev.slne.protect.bukkit.region.transaction;

import com.google.gson.JsonObject;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.transaction.core.transaction.TransactionData;
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
        jsonObject.addProperty("type", "buy");
        jsonObject.addProperty("region_id", region.getId());
        jsonObject.addProperty("world_name", world.getName());

        return jsonObject.toString();
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
