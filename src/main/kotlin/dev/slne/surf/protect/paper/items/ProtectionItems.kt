@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.items

import com.github.shynixn.mccoroutine.folia.launch
import com.jeff_media.morepersistentdatatypes.DataType
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.pdc.block.pdc
import dev.slne.surf.api.paper.util.namespacedKey
import dev.slne.surf.protect.paper.pdc.BlockPositionPersistentDataType
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.visual.MarkerCache
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.isInProtectionRegion
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

enum class ProtectionItems(val id: String, template: ItemStack, val cancelEvent: Boolean = true) {
    ACCEPT(id = "accept", template = buildItem(Material.LIME_CONCRETE) {
        displayName { primary("Kaufen") }
        buildLore {
            line { }
            line { info("Kaufe das Grundstück") }
            line { }
        }
    }) {
        override suspend fun handleInteract(
            user: ProtectionUser,
            regionCreation: ProtectionRegion,
            event: PlayerInteractEvent
        ) {
            regionCreation.finishProtection()
        }
    },
    CANCEL_PROTECTION(id = "cancel", template = buildItem(Material.RED_CONCRETE) {
        displayName { error("Abbrechen") }
        buildLore {
            line { }
            line { info("Beende die Grundstückserstellung.") }
            line { }
        }
    }) {
        override suspend fun handleInteract(
            user: ProtectionUser,
            regionCreation: ProtectionRegion,
            event: PlayerInteractEvent
        ) {
            regionCreation.cancelProtection()
        }
    },
    MARKER(id = "marker", cancelEvent = false, template = buildItem(Material.REDSTONE_TORCH) {
        displayName { primary("Marker") }
        buildLore {
            line { }
            line { info("Platziere die Marker um dein Grundstück zu definieren.") }
            line { }
        }
    }) {
        override fun handleBlockBreak(user: ProtectionUser, event: BlockBreakEvent) {
            val block = event.block
            val marker = MarkerCache.get(block.location.toBlock()) ?: return
            val markerRegionCreation = marker.regionCreation
            val userRegionCreation = user.regionCreation

            if (markerRegionCreation != userRegionCreation) {
                event.isCancelled = true
                return
            }

            event.isDropItems = false
            MarkerCache.invalidate(marker)

            plugin.launch {
                markerRegionCreation.removeMarker(marker)
                user.updateMarkerItems()
            }
        }

        override fun handleBlockPlace(user: ProtectionUser, event: BlockPlaceEvent) {
            val loc = event.blockPlaced.location
            val regionCreation = user.regionCreation

            if (regionCreation == null || !loc.isInProtectionRegion()) {
                event.isCancelled = true
                return
            }

            val currentData = event.blockReplacedState.blockData
            val marker = regionCreation.createMarker(
                loc.toBlock(),
                currentData,
                isExpanding = false
            )

            if (marker == null) {
                event.isCancelled = true
                return
            }

            plugin.launch { user.updateMarkerItems() }
        }
    };

    val item: ItemStack by lazy {
        template.apply {
            editPersistentDataContainer { pdc ->
                pdc.set(namespacedKey, pdcType, this@ProtectionItems)
            }
        }
    }

    open suspend fun handleInteract(
        user: ProtectionUser,
        regionCreation: ProtectionRegion,
        event: PlayerInteractEvent
    ) {
    }

    open fun handleBlockBreak(user: ProtectionUser, event: BlockBreakEvent) {
    }

    open fun handleBlockPlace(user: ProtectionUser, event: BlockPlaceEvent) {
    }

    companion object {
        private val namespacedKey = namespacedKey("protection-item")
        private val pdcType = DataType.asEnum(ProtectionItems::class.java)
        private val itemsnamespacedKey = namespacedKey("protection-items")
        private val itemsPdcType = DataType.asMap(BlockPositionPersistentDataType, pdcType)

        fun isProtectionItem(stack: ItemStack): Boolean {
            return stack.persistentDataContainer.has(namespacedKey, pdcType)
        }

        fun isProtectionBlock(block: Block): Boolean = block.pdc().has(namespacedKey)
        fun getProtectionItem(stack: ItemStack): ProtectionItems? =
            stack.persistentDataContainer.get(namespacedKey, pdcType)

        fun getProtectionBlock(block: Block): ProtectionItems? =
            block.pdc().get(namespacedKey, pdcType)

        fun makeProtectionBlock(item: ProtectionItems, block: Block) =
            block.pdc().set(namespacedKey, pdcType, item)

        fun removeProtectionBlock(block: Block) = block.pdc().remove(namespacedKey)
    }
}