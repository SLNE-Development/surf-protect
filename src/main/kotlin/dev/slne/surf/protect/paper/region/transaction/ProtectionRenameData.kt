package dev.slne.surf.protect.paper.region.transaction

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.util.regionContainer
import dev.slne.transaction.api.transaction.data.TransactionData

data class ProtectionRenameData(
    private var region: ProtectedRegion?,
    private var fromName: String?,
    private var toName: String?
) : TransactionData {
    override fun toJson() = JsonObject().apply {
        addProperty("type", "rename")
        addProperty("region_id", region?.id)
        addProperty("from_name", fromName)
        addProperty("to_name", toName)
    }.toString()


    override fun fromJson(json: String) {
        JsonParser.parseString(json).asJsonObject.apply {
            val regionId = get("region_id").asString
            region = regionContainer.loaded.asSequence()
                .mapNotNull { it.getRegion(regionId) }
                .first()

            fromName = get("from_name").asString
            toName = get("to_name").asString
        }
    }
}
