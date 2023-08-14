package dev.slne.protect.bukkit.region.transaction;

import com.google.gson.JsonObject;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.transaction.api.transaction.data.TransactionData;

public class ProtectionRenameData implements TransactionData {

    private final ProtectedRegion region;

    private String fromName;
    private String toName;

    /**
     * Construct a new {@link ProtectionRenameData}
     *
     * @param region   The region
     * @param fromName The old name
     * @param toName   The new name
     */
    public ProtectionRenameData(ProtectedRegion region, String fromName, String toName) {
        this.region = region;
        this.fromName = fromName;
        this.toName = toName;
    }

    @Override
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "rename");
        jsonObject.addProperty("region_id", region.getId());
        jsonObject.addProperty("from_name", fromName);
        jsonObject.addProperty("to_name", toName);

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
