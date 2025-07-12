@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.util

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.Vector2
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import com.sk89q.worldguard.protection.regions.RegionContainer
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter
import com.sk89q.worldedit.util.Location as WorldEditLocation
import com.sk89q.worldedit.world.World as WorldEditWorld
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.toObjectList
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.Position
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

fun ProtectedRegion.toRegionOrNull(): Region? = WorldEditRegionConverter.convertToRegion(this)
fun ProtectedRegion.toRegion(): Region =
    toRegionOrNull() ?: error("Failed to convert ProtectedRegion to Region: $this")

fun ProtectedRegion.fastCenter(): BlockVector3 = minimumPoint.add(maximumPoint).divide(2)

fun Vector2.toBlockPosition(y: Int = 0) =
    Position.block(x().fastFloorToInt(), y, z().fastFloorToInt())

fun BlockVector2.toBlockPosition(y: Int = 0) = Position.block(x(), y, z())

fun UUID.toLocalPlayer(): LocalPlayer {
    val player = Bukkit.getPlayer(this)
    return if (player != null && player.isOnline) {
        WorldGuardPlugin.inst().wrapPlayer(player)
    } else {
        WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(this))
    }
}

val regionContainer: RegionContainer get() = WorldGuard.getInstance().platform.regionContainer
fun World.getRegionManagerOrNull() = regionContainer.get(toWorldEdit())
fun World.getRegionManager() =
    getRegionManagerOrNull() ?: error("Region manager not found for world: ${this.name}")

fun World.toWorldEdit(): com.sk89q.worldedit.world.World = BukkitAdapter.adapt(this)

fun ProtectedRegion.getRegionManagerOrNull(): RegionManager? =
    regionContainer.loaded.find { it.hasRegion(id) }

fun ProtectedRegion.getRegionManager(): RegionManager =
    getRegionManagerOrNull() ?: error("Region manager not found for region: ${this.id}")

// copy the volume calculation from WorldEdit, since the worldguard developer is incapable of doing it --> https://github.com/EngineHub/WorldGuard/pull/1930
fun ProtectedRegion.fixedVolume(): Long {
    val n = points.size
    if (n < 3) return 0L

    var twiceArea = 0L      // 2 × actual area (can be negative)
    var j = n - 1
    for (i in 0 until n) {
        val pi = points[i]
        val pj = points[j]
        twiceArea += (pj.x() + pi.x()).toLong() * (pj.z() - pi.z())
        j = i
    }

    val baseArea = abs(twiceArea) / 2L
    val height = (maximumPoint.y() - minimumPoint.y() + 1).toLong()
    return baseArea * height
}

fun String.toLocalPlayer(): LocalPlayer {
    val player = Bukkit.getPlayerExact(this)
    return if (player != null && player.isOnline) {
        WorldGuardPlugin.inst().wrapPlayer(player)
    } else {
        WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(this))
    }
}

fun LocalPlayer.allRegions() = regionContainer.loaded.asSequence()
    .flatMap {
        it.regions.asSequence()
            .filter { (_, region) -> region.owners.contains(this) }
    }
    .map { it.value }
    .toObjectList()

fun Location.isInProtectionRegion(): Boolean {
    val manager = world.getRegionManagerOrNull() ?: return false

    val regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(this))
    return regions.testState(null, ProtectionFlagsRegistry.SURF_PROTECT)
}

fun Location.isGlobalRegion(): Boolean {
    val manager = world.getRegionManagerOrNull() ?: return true

    val regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(this))
    return regions.size() == 0
}

val WorldEditLocation.world: WorldEditWorld
    get() = this.extent as? WorldEditWorld ?: error("Extent is not a World: $extent")

fun WorldEditLocation.toBukkitLocation(): Location {
    return BukkitAdapter.adapt(this)
}

fun Player.standsInProtectedRegion(region: ProtectedRegion) =
    location.getProtectedRegions().any { it.id == region.id }

fun ProtectedRegion.getMemberNames() = members.uniqueIds.asSequence()
    .map { Bukkit.getOfflinePlayer(it) }
    .map { it.name ?: it.uniqueId.toString() }
    .toObjectList()

fun ProtectedRegion.getOwnerNames() = owners.uniqueIds.asSequence()
    .map { Bukkit.getOfflinePlayer(it) }
    .map { it.name ?: it.uniqueId.toString() }
    .toObjectList()

@JvmOverloads
fun Location.getProtectedRegions(withGlobalRegion: Boolean = false): Set<ProtectedRegion> {
    val loc = BukkitAdapter.adapt(this)
    val regions = regionContainer.createQuery().getApplicableRegions(loc)

    return if (withGlobalRegion) {
        regions.regions
    } else {
        regions.regions.filterNotTo(mutableObjectSetOf()) { it.id == ProtectedRegion.GLOBAL_REGION }
    }
}


fun BlockPosition.distanceSquared(other: BlockPosition): Int {
    val dx = this.blockX() - other.blockX()
    val dy = this.blockY() - other.blockY()
    val dz = this.blockZ() - other.blockZ()
    return dx * dx + dy * dy + dz * dz
}

fun Position.distanceSquared(other: Position): Double {
    val dx = this.x() - other.x()
    val dy = this.y() - other.y()
    val dz = this.z() - other.z()
    return dx * dx + dy * dy + dz * dz
}

suspend fun World.getChunkSnapshotAsync(
    blockX: Int,
    blockZ: Int,
    includeMaxblockY: Boolean = true,
    includeBiome: Boolean = false,
    includeBiomeTempRain: Boolean = false,
    includeLightData: Boolean = false
) = getChunkAtAsync(blockX shr 4, blockZ shr 4).await()
    .getChunkSnapshot(includeMaxblockY, includeBiome, includeBiomeTempRain, includeLightData)

fun ChunkSnapshot.getBlockDataAt(
    blockX: Int,
    blockY: Int,
    blockZ: Int
): BlockData {
    return getBlockData(blockX and 15, blockY, blockZ and 15)
}