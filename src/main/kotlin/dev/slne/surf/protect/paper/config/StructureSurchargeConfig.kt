package dev.slne.surf.protect.paper.config

import net.kyori.adventure.key.Key
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class StructureSurchargeConfig(
    @Comment("Surcharge per block² of protection area that lies on the footprint of the structure. 0 = no surcharge")
    val surcharges: Map<String, Double> = emptyMap()
) {
    /**
     * All structures with a surcharge greater than 0, keyed by their full structure key
     * (e.g. `minecraft:trail_ruins`). Entries with an invalid key are ignored.
     */
    val pricedStructures: Map<String, Double>
        get() = buildMap {
            for ((key, surcharge) in surcharges) {
                if (surcharge <= 0.0 || !Key.parseable(key)) continue
                put(Key.key(key).asString(), surcharge)
            }
        }
}
