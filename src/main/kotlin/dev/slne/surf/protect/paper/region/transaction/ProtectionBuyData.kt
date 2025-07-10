package dev.slne.surf.protect.paper.region.transaction

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.transaction.api.transaction.data.TransactionData
import org.bukkit.Bukkit
import org.bukkit.World

data class ProtectionBuyData(private var world: World?, private var region: ProtectedRegion?) :
    TransactionData {
    override fun toJson() = JsonObject().apply {
        addProperty("type", "buy")
        region?.let { addProperty("region_id", it.id) }
        world?.let { addProperty("world_name", it.name) }
    }.toString()

    override fun fromJson(json: String) {
        JsonParser.parseString(json).asJsonObject.apply {
            world = Bukkit.getWorld(get("world_name").asString)
            world?.getRegionManagerOrNull()?.let { regionManager ->
                region = regionManager.getRegion(get("region_id").asString)
            }
        }
    }
}
