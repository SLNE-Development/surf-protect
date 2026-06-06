package dev.slne.surf.protect.paper.config

import dev.slne.surf.api.core.config.constraints.PositiveNumber
import dev.slne.surf.api.core.config.constraints.Range
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.transaction.api.currency.Currency
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockType
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import pl.allegro.finance.tradukisto.ValueConverters
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
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
    val dirtyMarkers: MutableList<DirtyMarker> = mutableListOf(),
    val awaitingProtectionModes: MutableList<AwaitingProtectionModeConfig> = mutableListOf()
) {

    @ConfigSerializable
    data class ProtectionSettings(
        val maxDistanceFromStart: Double = 100.0,
        val retailPercent: Int = 65,
        val renamePrice: Int = 2_500,
        @PositiveNumber
        val minNameLength: Int = 3,
        @PositiveNumber
        val maxNameLength: Int = 22,
        val protectOnlyAboveNetherRoofInNether: Boolean = true,
        val canEnterProtectionModeBelowNetherRoofInNether: Boolean = false,

        val iconByEnvironment: Map<World.Environment, ItemType> = mapOf(
            World.Environment.NORMAL to ItemType.GRASS_BLOCK,
            World.Environment.NETHER to ItemType.CRIMSON_NYLIUM,
            World.Environment.THE_END to ItemType.END_STONE,
        )
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
        val spawnProtectionPerBlock: Double = 200.0,

        @Range(min = 0.0, max = 100.0)
        val discountPercent: Int = 20,

        val environmentMultiplier: Map<World.Environment, Double> = mapOf(
            World.Environment.NORMAL to 1.0,
            World.Environment.NETHER to 2.0,
        )
    ) {
        val discountModifier: Double
            get() = 1.0 - (discountPercent / 100.0)
    }

    @ConfigSerializable
    data class CurrencyConfig(
        val name: String = "CastCoin"
    ) {
        val currency: Currency get() = Currency[name] ?: error("Currency with name '$name' not found")
    }

    @ConfigSerializable
    class AwaitingProtectionModeConfig(val data: String = "") {
        val playerUuid: UUID
            get() = decodeStream { din ->
                UUID(din.readLong(), din.readLong())
            }

        val inventory: Array<ItemStack>
            get() = decodeStream { din ->
                skipUUID(din)
                din.readUTF() // world name
                din.readDouble() // x
                din.readDouble() // y
                din.readDouble() // z
                din.readFloat() // yaw
                val invLen = din.readInt()
                val invBytes = ByteArray(invLen)
                din.readFully(invBytes)
                ItemStack.deserializeItemsFromBytes(invBytes)
            }

        val startLocation: Location
            get() = decodeStream { din ->
                skipUUID(din)
                val worldName = din.readUTF()
                val x = din.readDouble()
                val y = din.readDouble()
                val z = din.readDouble()
                val yaw = din.readFloat()
                Location(
                    server.getWorld(worldName) ?: error("World '$worldName' not found"),
                    x,
                    y,
                    z,
                    yaw,
                    0f
                )
            }

        private fun <T> decodeStream(block: (DataInputStream) -> T): T {
            val bytes = Base64.getDecoder().decode(data)
            return DataInputStream(ByteArrayInputStream(bytes)).use { din ->
                val version = din.readByte()
                require(version == VERSION) { "Unknown AwaitingProtectionModeConfig version: $version" }
                block(din)
            }
        }

        companion object {
            private const val VERSION: Byte = 1

            private fun skipUUID(din: DataInputStream) {
                din.readLong() // most significant bits
                din.readLong() // least significant bits
            }

            fun create(
                playerUuid: UUID,
                inventory: Array<ItemStack?>,
                location: Location
            ): AwaitingProtectionModeConfig {
                val invItems = Array(inventory.size) { i -> inventory[i] ?: ItemStack.empty() }
                val invBytes = ItemStack.serializeItemsAsBytes(invItems)
                val bytes = ByteArrayOutputStream().use { bout ->
                    DataOutputStream(bout).use { dout ->
                        dout.writeByte(VERSION.toInt())
                        dout.writeLong(playerUuid.mostSignificantBits)
                        dout.writeLong(playerUuid.leastSignificantBits)
                        dout.writeUTF(location.world.name)
                        dout.writeDouble(location.x)
                        dout.writeDouble(location.y)
                        dout.writeDouble(location.z)
                        dout.writeFloat(location.yaw)
                        dout.writeInt(invBytes.size)
                        dout.write(invBytes)
                    }
                    bout.toByteArray()
                }
                return AwaitingProtectionModeConfig(Base64.getEncoder().encodeToString(bytes))
            }
        }
    }

    @ConfigSerializable
    class DirtyMarker(val data: String = "") {
        val location: Location
            get() = decodeStream { din ->
                val worldName = din.readUTF()
                val x = din.readInt()
                val y = din.readInt()
                val z = din.readInt()
                Location(
                    server.getWorld(worldName) ?: error("World '$worldName' not found"),
                    x.toDouble(),
                    y.toDouble(),
                    z.toDouble()
                )
            }

        val blockData: BlockData
            get() = decodeStream { din ->
                din.readUTF() // world name (skip)
                din.readInt() // x (skip)
                din.readInt() // y (skip)
                din.readInt() // z (skip)
                server.createBlockData(din.readUTF())
            }

        private fun <T> decodeStream(block: (DataInputStream) -> T): T {
            val bytes = Base64.getDecoder().decode(data)
            return DataInputStream(ByteArrayInputStream(bytes)).use { din ->
                val version = din.readByte()
                require(version == VERSION) { "Unknown DirtyMarker version: $version" }
                block(din)
            }
        }

        companion object {
            private const val VERSION: Byte = 1

            fun create(location: Location, blockData: BlockData): DirtyMarker {
                val bytes = ByteArrayOutputStream().use { bout ->
                    DataOutputStream(bout).use { dout ->
                        dout.writeByte(VERSION.toInt())
                        dout.writeUTF(location.world.name)
                        dout.writeInt(location.blockX)
                        dout.writeInt(location.blockY)
                        dout.writeInt(location.blockZ)
                        dout.writeUTF(blockData.asString)
                    }
                    bout.toByteArray()
                }
                return DirtyMarker(Base64.getEncoder().encodeToString(bytes))
            }
        }
    }
}