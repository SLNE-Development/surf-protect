@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.items

import com.github.shynixn.mccoroutine.folia.launch
import com.jeff_media.morepersistentdatatypes.DataType
import dev.slne.surf.protect.paper.pdc.BlockPositionPersistentDataType
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.region.visual.MarkerCache
import dev.slne.surf.protect.paper.user.ProtectionUser
import dev.slne.surf.protect.paper.util.isInProtectionRegion
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.pdc.block.pdc
import dev.slne.surf.surfapi.bukkit.api.util.key
import kotlinx.coroutines.launch
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
                launch { markerRegionCreation.removeMarker(marker) }
                launch { user.updateMarkerItems() }
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
                pdc.set(key, pdcType, this@ProtectionItems)
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
        private val key = key("protection-item")
        private val pdcType = DataType.asEnum(ProtectionItems::class.java)
        private val itemsKey = key("protection-items")
        private val itemsPdcType = DataType.asMap(BlockPositionPersistentDataType, pdcType)

        fun isProtectionItem(stack: ItemStack): Boolean {
            return stack.persistentDataContainer.has(key, pdcType)
        }

        fun isProtectionBlock(block: Block): Boolean {
            return block.pdc().has(key)
            val pdc =
                block.chunk.persistentDataContainer.get(itemsKey, itemsPdcType) ?: return false
            return pdc.containsKey(block.location.toBlock())
        }

        fun getProtectionItem(stack: ItemStack): ProtectionItems? {
            return stack.persistentDataContainer.get(key, pdcType)
        }

        fun getProtectionBlock(block: Block): ProtectionItems? {
//            val pdc = block.chunk.persistentDataContainer.get(itemsKey, itemsPdcType) ?: return null
//            return pdc[block.location.toBlock()]
            return block.pdc().get(key, pdcType)
        }

        fun makeProtectionBlock(item: ProtectionItems, block: Block) {
//            val pdc = block.chunk.persistentDataContainer.getOrDefault(
//                itemsKey,
//                itemsPdcType,
//                mutableMapOf()
//            )
//            pdc[block.location.toBlock()] = item
//            block.chunk.persistentDataContainer.set(itemsKey, itemsPdcType, pdc)
            block.pdc().set(key, pdcType, item)
        }

        fun removeProtectionBlock(block: Block) {
//            val pdc = block.chunk.persistentDataContainer.get(itemsKey, itemsPdcType) ?: return
//            pdc.remove(block.location.toBlock())
//            block.chunk.persistentDataContainer.set(itemsKey, itemsPdcType, pdc)
            block.pdc().remove(key)
        }
    }
}