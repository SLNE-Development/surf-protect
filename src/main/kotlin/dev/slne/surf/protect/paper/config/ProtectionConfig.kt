package dev.slne.surf.protect.paper.config

import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import dev.slne.transaction.api.TransactionApi
import dev.slne.transaction.api.currency.Currency
import org.bukkit.block.BlockType
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import pl.allegro.finance.tradukisto.ValueConverters

val config by lazy {
    surfConfigApi.createSpongeYmlConfig<ProtectionConfig>(plugin.dataPath, "config.yml")
}

@ConfigSerializable
data class ProtectionConfig(
    val protection: ProtectionSettings = ProtectionSettings(),
    val cooldown: CooldownSettings = CooldownSettings(),
    val area: AreaSettings = AreaSettings(),
    val markers: MarkerSettings = MarkerSettings(),
    val pricing: PricingSettings = PricingSettings(),
    val currency: CurrencyConfig = CurrencyConfig(),
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
        val currency: Currency = TransactionApi.getCurrency(name)
            .orElseThrow { IllegalArgumentException("Currency with name '$name' not found") }
    }
}