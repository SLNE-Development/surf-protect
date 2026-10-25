package dev.slne.surf.protect.paper.region.structure

import net.kyori.adventure.key.Key

/**
 * The surcharge for structures that lie inside a protection.
 *
 * @property parts the surcharge per structure, the most expensive first
 */
data class StructureSurcharge(val parts: List<Part>) {
    val totalBlocks: Long get() = parts.sumOf { it.blocks }
    val totalCost: Double get() = parts.sumOf { it.cost }

    /**
     * @property structureKey the full key of the structure, e.g. `nova_structures:desert_ruins`
     * @property blocks the amount of block² of the protection on the footprint of the structure
     * @property pricePerBlock the surcharge per block² of this structure
     */
    data class Part(
        val structureKey: String,
        val blocks: Long,
        val pricePerBlock: Double
    ) {
        val cost: Double get() = blocks * pricePerBlock
        val displayName: String get() = Key.key(structureKey).value().replace('_', ' ')
    }

    companion object {
        val NONE = StructureSurcharge(emptyList())
    }
}
