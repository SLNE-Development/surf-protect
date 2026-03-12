package dev.slne.surf.protect.paper.user

import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

data class PendingMarkerData(
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val blockData: String,
)

data class PendingResetEntry(
    val worldName: String,
    val startX: Double,
    val startY: Double,
    val startZ: Double,
    val startYaw: Float,
    val startPitch: Float,
    val inventoryContent: Array<ItemStack?>,
    val markers: List<PendingMarkerData>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PendingResetEntry) return false
        return worldName == other.worldName
                && startX == other.startX
                && startY == other.startY
                && startZ == other.startZ
                && startYaw == other.startYaw
                && startPitch == other.startPitch
                && inventoryContent.contentEquals(other.inventoryContent)
                && markers == other.markers
    }

    override fun hashCode(): Int {
        var result = worldName.hashCode()
        result = 31 * result + startX.hashCode()
        result = 31 * result + startY.hashCode()
        result = 31 * result + startZ.hashCode()
        result = 31 * result + startYaw.hashCode()
        result = 31 * result + startPitch.hashCode()
        result = 31 * result + inventoryContent.contentHashCode()
        result = 31 * result + markers.hashCode()
        return result
    }
}

object PendingProtectResetManager {
    private const val CONFIG_FILE_NAME = "config.yml"
    private const val PENDING_RESETS_KEY = "pending-resets"
    private val pendingResets = ConcurrentHashMap<UUID, PendingResetEntry>()

    fun add(uuid: UUID, entry: PendingResetEntry) {
        pendingResets[uuid] = entry
    }

    fun remove(uuid: UUID): PendingResetEntry? = pendingResets.remove(uuid)

    operator fun contains(uuid: UUID): Boolean = uuid in pendingResets

    fun save(dataFolder: File) {
        dataFolder.mkdirs()
        val configFile = File(dataFolder, CONFIG_FILE_NAME)
        val loader = YamlConfigurationLoader.builder()
            .file(configFile)
            .build()

        val root = if (configFile.exists()) loader.load() else loader.createNode()
        val pendingNode = root.node(PENDING_RESETS_KEY)
        pendingNode.set(null)

        val encoder = Base64.getEncoder()
        for ((uuid, entry) in pendingResets) {
            val entryNode = pendingNode.node(uuid.toString())

            entryNode.node("world-name").set(entry.worldName)
            entryNode.node("start-x").set(entry.startX)
            entryNode.node("start-y").set(entry.startY)
            entryNode.node("start-z").set(entry.startZ)
            entryNode.node("start-yaw").set(entry.startYaw.toDouble())
            entryNode.node("start-pitch").set(entry.startPitch.toDouble())
            entryNode.node("inventory-size").set(entry.inventoryContent.size)

            val invNode = entryNode.node("inventory")
            for ((index, item) in entry.inventoryContent.withIndex()) {
                if (item != null && item.type != Material.AIR) {
                    invNode.node(index.toString()).set(
                        encoder.encodeToString(item.serializeAsBytes())
                    )
                }
            }

            val markersNode = entryNode.node("markers")
            for ((index, marker) in entry.markers.withIndex()) {
                val markerNode = markersNode.node(index.toString())
                markerNode.node("world").set(marker.world)
                markerNode.node("x").set(marker.x)
                markerNode.node("y").set(marker.y)
                markerNode.node("z").set(marker.z)
                markerNode.node("block-data").set(marker.blockData)
            }
        }

        loader.save(root)
    }

    fun load(dataFolder: File) {
        val configFile = File(dataFolder, CONFIG_FILE_NAME)
        if (!configFile.exists()) return

        val loader = YamlConfigurationLoader.builder()
            .file(configFile)
            .build()

        val root = loader.load()
        val pendingNode = root.node(PENDING_RESETS_KEY)
        if (pendingNode.isNull || pendingNode.virtual()) return

        pendingResets.clear()
        val decoder = Base64.getDecoder()

        for ((uuidKey, entryNode) in pendingNode.childrenMap()) {
            val uuid = runCatching { UUID.fromString(uuidKey.toString()) }.getOrNull() ?: continue

            val worldName = entryNode.node("world-name").string ?: continue
            val startX = entryNode.node("start-x").getDouble(0.0)
            val startY = entryNode.node("start-y").getDouble(0.0)
            val startZ = entryNode.node("start-z").getDouble(0.0)
            val startYaw = entryNode.node("start-yaw").getDouble(0.0).toFloat()
            val startPitch = entryNode.node("start-pitch").getDouble(0.0).toFloat()
            val invSize = entryNode.node("inventory-size").getInt(41)

            val inventory = arrayOfNulls<ItemStack>(invSize)
            val invNode = entryNode.node("inventory")
            if (!invNode.isNull && !invNode.virtual()) {
                for ((slotKey, itemNode) in invNode.childrenMap()) {
                    val slot = slotKey.toString().toIntOrNull() ?: continue
                    if (slot < 0 || slot >= invSize) continue
                    val base64 = itemNode.string ?: continue
                    inventory[slot] = runCatching {
                        ItemStack.deserializeBytes(decoder.decode(base64))
                    }.getOrNull()
                }
            }

            val markers = mutableListOf<PendingMarkerData>()
            val markersNode = entryNode.node("markers")
            if (!markersNode.isNull && !markersNode.virtual()) {
                for ((_, markerNode) in markersNode.childrenMap()) {
                    val markerWorld = markerNode.node("world").string ?: continue
                    val markerX = markerNode.node("x").getInt(0)
                    val markerY = markerNode.node("y").getInt(0)
                    val markerZ = markerNode.node("z").getInt(0)
                    val markerBlockData = markerNode.node("block-data").string ?: continue
                    markers.add(PendingMarkerData(markerWorld, markerX, markerY, markerZ, markerBlockData))
                }
            }

            pendingResets[uuid] = PendingResetEntry(
                worldName = worldName,
                startX = startX,
                startY = startY,
                startZ = startZ,
                startYaw = startYaw,
                startPitch = startPitch,
                inventoryContent = inventory,
                markers = markers,
            )
        }
    }
}
