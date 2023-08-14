package dev.slne.protect.bukkit.region.transaction;

import com.google.gson.JsonObject;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.transaction.api.transaction.data.TransactionData;

public class ProtectionSellData implements TransactionData {

    private final ProtectedRegion region;

    /**
     * Construct a new {@link ProtectionSellData}
     *
     * @param region The region
     */
    public ProtectionSellData(ProtectedRegion region) {
        this.region = region;
    }

    @Override
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "sell");
        jsonObject.addProperty("region_id", region.getId());

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
