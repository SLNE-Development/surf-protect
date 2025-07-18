@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.region.visual

import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.sk89q.worldedit.math.BlockVector2
import dev.slne.surf.protect.paper.items.ProtectionItems
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import io.papermc.paper.math.BlockPosition
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.World
import org.bukkit.block.data.BlockData
import java.lang.ref.WeakReference

data class Marker(
    val world: WeakReference<World>,
    val regionCreation: ProtectionRegion,
    val pos: BlockPosition,
    val previousData: BlockData?,
) {
    val blockX = pos.blockX()
    val blockY = pos.blockY()
    val blockZ = pos.blockZ()

    val chunkX = blockX shr 4
    val chunkZ = blockZ shr 4

    val chunkBlockX = blockX and 15
    val chunkBlockZ = blockZ and 15

    @Volatile
    private var restored = false

    init {
        MarkerCache.put(this)
    }

    fun isRestored(): Boolean = restored

    suspend fun place() {
        val markerBlockData = ProtectionItems.MARKER.item.type.asBlockType()?.createBlockData()
        if (markerBlockData == null) return
        val world = this.world.get() ?: return

        val chunk = world.getChunkAtAsync(chunkX, chunkZ).await()
        withContext(plugin.regionDispatcher(world, chunk.x, chunk.z)) {
            val block = chunk.getBlock(chunkBlockX, blockY, chunkBlockZ)
            block.blockData = markerBlockData
            ProtectionItems.makeProtectionBlock(ProtectionItems.MARKER, block)
        }
    }

    suspend fun restorePreviousData() {
        if (restored || previousData == null) return
        val world = this.world.get() ?: return

        restored = true
        MarkerCache.invalidate(this)

        val chunk = world.getChunkAtAsync(chunkX, chunkZ).await()
        withContext(plugin.regionDispatcher(world, chunk.x, chunk.z)) {
            chunk.getBlock(chunkBlockX, blockY, chunkBlockZ).blockData = previousData
        }
    }

    fun toBlockVector2(): BlockVector2 = BlockVector2.at(blockX, blockZ)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Marker) return false

        if (blockX != other.blockX) return false
        if (blockY != other.blockY) return false
        if (blockZ != other.blockZ) return false
        if (regionCreation != other.regionCreation) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockX
        result = 31 * result + blockY
        result = 31 * result + blockZ
        result = 31 * result + regionCreation.hashCode()
        return result
    }
}
