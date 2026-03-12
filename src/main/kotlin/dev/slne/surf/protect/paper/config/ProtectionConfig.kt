package dev.slne.surf.protect.paper.config

import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.transaction.api.currency.Currency
import org.bukkit.Location
import org.bukkit.block.BlockType
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import pl.allegro.finance.tradukisto.ValueConverters
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.*

@ConfigSerializable
data class ProtectionConfig(
    val protection: ProtectionSettings = ProtectionSettings(),
    val cooldown: CooldownSettings = CooldownSettings(),
    val area: AreaSettings = AreaSettings(),
    val markers: MarkerSettings = MarkerSettings(),
    val pricing: PricingSettings = PricingSettings(),
    val currency: CurrencyConfig = CurrencyConfig(),
    val awaitingProtectionModes: MutableList<AwaitingProtectionModeConfig> = mutableListOf()
) {

    @ConfigSerializable
    data class ProtectionSettings(
        val maxDistanceFromStart: Double = 100.0,
        val retailPercent: Int = 65,
        val renamePrice: Int = 2_500
    ) {
        val retailModifier: Double
            get() = retailPercent / 100.0

        init {
            require(retailPercent in 0..100) { "Retail percent must be between 0 and 100" }
        }
    }

    @ConfigSerializable
    data class CooldownSettings(
        val baseMs: Long = 300_000,
        val maxMs: Long = 3_600_000
    )

    @ConfigSerializable
    data class AreaSettings(
        val minBlocks: Long = 250,
        val maxBlocks: Long = Int.MAX_VALUE.toLong()
    )

    @ConfigSerializable
    data class MarkerSettings(
        val amount: Int = 8,
        val minAmount: Int = 4,
        val creationBlockData: String = BlockType.PALE_OAK_PRESSURE_PLATE.createBlockData().asString,
        val expandingBlockData: String = BlockType.POLISHED_DEEPSLATE_SLAB.createBlockData().asString,
    ) {
        val amountWritten: String = ValueConverters.GERMAN_INTEGER.asWords(amount)
        val creationBlockDataParsed = server.createBlockData(creationBlockData)
        val expandingBlockDataParsed = server.createBlockData(expandingBlockData)

        init {
            require(amount >= minAmount) { "Marker amount ($amount) must be greater than or equal to minimum markers ($minAmount)" }
            require(minAmount >= 3) { "Minimum markers must be at least 3" }
        }
    }

    @ConfigSerializable
    data class PricingSettings(
        val minPerBlock: Double = 4.0,
        val spawnProtectionPerBlock: Double = 200.0
    )

    @ConfigSerializable
    data class CurrencyConfig(
        val name: String = "CastCoin"
    ) {
        val currency: Currency = Currency[name] ?: error("Currency with name '$name' not found")
    }

    @ConfigSerializable
    class AwaitingProtectionModeConfig(
        val playerUuidString: String,
        val inventoryBytes: ByteArray,
        val startLocationString: String
    ) {
        val playerUuid: UUID = UUID.fromString(playerUuidString)
        val inventory = ItemStack.deserializeItemsFromBytes(inventoryBytes)
        val startLocation: Location = startLocationString.toLocation()

        fun String.toLocation(): Location {
            val parts = this.split(",")
            require(parts.size == 5) { "Invalid location string: $this" }
            val worldName = parts[0]
            val x =
                parts[1].toDoubleOrNull() ?: error("Invalid X coordinate in location string: $this")
            val y =
                parts[2].toDoubleOrNull() ?: error("Invalid Y coordinate in location string: $this")
            val z =
                parts[3].toDoubleOrNull() ?: error("Invalid Z coordinate in location string: $this")
            val yaw = parts[4].toFloatOrNull() ?: error("Invalid yaw in location string: $this")
            return Location(
                server.getWorld(worldName) ?: error("World '$worldName' not found"),
                x,
                y,
                z,
                yaw,
                0f
            )
        }
    }
}

fun Location.asString(): String = "${world.name},$x,$y,$z,$yaw"

fun List<ItemStack>.serializeItemsToBytes(): ByteArray =
    ByteArrayOutputStream().use { arrayOut ->
        DataOutputStream(arrayOut).use { dataOut ->
            dataOut.writeByte(1)
            dataOut.writeInt(this.size)

            for (item in this) {
                if (item.isEmpty) {
                    dataOut.writeInt(0)
                } else {
                    val itemBytes = item.serializeAsBytes()
                    dataOut.writeInt(itemBytes.size)
                    dataOut.write(itemBytes)
                }
            }
        }
        arrayOut.toByteArray()
    }