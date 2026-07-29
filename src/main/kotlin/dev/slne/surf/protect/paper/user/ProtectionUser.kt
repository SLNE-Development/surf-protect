@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.user

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.protect.paper.config
import dev.slne.surf.protect.paper.config.ProtectionConfig
import dev.slne.surf.protect.paper.configManager
import dev.slne.surf.protect.paper.items.ProtectionItems
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.util.fastCenter
import dev.slne.surf.protect.paper.util.isInProtectionRegion
import dev.slne.surf.protect.paper.util.toLocalPlayer
import dev.slne.surf.transaction.api.user.TransactionUser
import io.papermc.paper.math.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class ProtectionUser(val uuid: UUID) {

    /** The region that is currently being created, or *null* if none. */
    var regionCreation: ProtectionRegion? = null
        private set

    val isCreatingRegion: Boolean
        get() = this.regionCreation != null


    val localPlayer: LocalPlayer get() = uuid.toLocalPlayer()
    val bukkitPlayer get() = Bukkit.getPlayer(uuid)

    private val protectionModeCooldown = ProtectionCooldownTracker()

    val transactionUser get() = TransactionUser[uuid]

    suspend fun startRegionCreation(
        newRegion: ProtectionRegion
    ): Boolean {
        val player = this.bukkitPlayer ?: return false

        when {
            isCreatingRegion -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du befindest dich bereits im Protection-Mode.")
                }
                return false
            }

            !player.location.isInProtectionRegion() -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Protection-Mode hier nicht betreten.")
                }
                return false
            }

            protectionModeCooldown.onCooldown -> {
                val left = protectionModeCooldown.timeLeft.milliseconds
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Protection-Mode erst wieder in ")
                    append(
                        CommonComponents.formatTime(
                            left,
                            showSeconds = true,
                            shortForms = false,
                            separator = Component.text(", ", Colors.VARIABLE_VALUE)
                        )
                    )
                    error(" betreten.")
                }
                return false
            }

            !config.protection.canEnterProtectionModeBelowNetherRoofInNether
                    && player.world.environment == World.Environment.NETHER
                    && player.y < player.world.logicalHeight -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du kannst den Protection-Mode nicht unterhalb der Netherdecke betreten.")
                }
                return false
            }

            @Suppress("DEPRECATION")
            !player.isOnGround -> {
                player.sendText {
                    appendErrorPrefix()
                    error("Du musst auf dem Boden sein, um den Protection-Mode zu betreten.")
                }
                return false
            }
        }


        val worldBorder = server.createWorldBorder()
        val (centerPos, size) = computeWorldBorderParams(player, newRegion)

        withContext(Dispatchers.IO) {
            saveAwaitingProtectionMode(newRegion)
        }

        worldBorder.setCenter(centerPos.x(), centerPos.z())
        worldBorder.size = size * 2 // diameter
        worldBorder.warningDistance = 0
        newRegion.worldBorderSize = size

        withContext(plugin.entityDispatcher(player)) {
            with(player) {
                allowFlight = true
                isFlying = true
                flySpeed = 0.3f
                isCollidable = false
                this.worldBorder = worldBorder

                with(inventory) {
                    clear()
                    setItem(0, ProtectionItems.MARKER.item.asQuantity(newRegion.markerCountLeft))
                    setItem(7, ProtectionItems.ACCEPT.item)
                    setItem(8, ProtectionItems.CANCEL_PROTECTION.item)
                }
            }
        }

        PacketEvents.getAPI().playerManager.sendPacketSilently( // TODO: 08.07.2025 23:56 - replace with surf api
            player,
            WrapperPlayWorldBorderLerpSize(size * 2, size * 2 - BORDER_LERP_OFFSET, Long.MAX_VALUE)
        )

        this.regionCreation = newRegion
        return true
    }

    suspend fun resetRegionCreation(aborted: Boolean, shutdown: Boolean = false) {
        val creation = regionCreation ?: return
        this.regionCreation = null

        if (aborted) {
            protectionModeCooldown.recordAbort()
        } else {
            protectionModeCooldown.reset()
        }

        if (shutdown) return

        removeAwaitingProtectionMode()

        val player = this.bukkitPlayer ?: return
        withContext(plugin.entityDispatcher(player)) {
            restorePlayerProperties(
                player,
                creation.startingInventoryContent.map { it ?: ItemStack.empty() }.toTypedArray()
            )
        }

        player.teleportAsync(creation.startLocation)
    }

    private fun saveAwaitingProtectionMode(region: ProtectionRegion) {
        configManager.edit {
            awaitingProtectionModes.removeIf { it.playerUuid == uuid }
            awaitingProtectionModes.add(
                ProtectionConfig.AwaitingProtectionModeConfig.create(
                    playerUuid = uuid,
                    inventory = region.startingInventoryContent,
                    location = region.startLocation
                )
            )
        }
    }

    fun removeAwaitingProtectionMode() {
        if (config.awaitingProtectionModes.none { it.playerUuid == uuid }) return

        configManager.edit {
            awaitingProtectionModes.removeIf { it.playerUuid == uuid }
        }
    }

    fun restorePlayerProperties(player: Player, inventory: Array<ItemStack>) {
        with(player) {
            fallDistance = 0f
            player.inventory.contents = inventory
            allowFlight = gameMode == GameMode.CREATIVE
            isFlying = gameMode == GameMode.CREATIVE
            flySpeed = 0.2f
            isCollidable = true
            worldBorder = null
        }
    }

    suspend fun updateMarkerItems() {
        val player = this.bukkitPlayer ?: return
        val regionCreation = this.regionCreation ?: return

        withContext(plugin.entityDispatcher(player)) {
            val item = ProtectionItems.MARKER.item.asQuantity(regionCreation.markerCountLeft)
            player.inventory.setItem(0, item)
        }
    }

    private fun computeWorldBorderParams(
        player: Player,
        region: ProtectionRegion
    ): Pair<Position, Double> {
        val expanding = region.expandingProtection
        return if (expanding != null) {
            val center = expanding.fastCenter().toBlockVector2()
            val size =
                config.protection.maxDistanceFromStart + maxDistanceFromCenter(expanding, center)
            Position.block(center.x(), 0, center.z()) to size
        } else {
            player.location to config.protection.maxDistanceFromStart
        }
    }

    private fun maxDistanceFromCenter(region: ProtectedRegion, center: BlockVector2): Double {
        return region.points.maxOf { point ->
            maxOf(
                abs(point.x() - center.x()),
                abs(point.z() - center.z())
            ).toDouble()
        }
    }

    fun sendMessage(message: Component) {
        this.bukkitPlayer?.sendMessage(message)
    }

    fun handleQuit(player: Player) {
        val regionCreation = regionCreation
        if (regionCreation != null) {
            removeAwaitingProtectionMode()
            restorePlayerProperties(
                player,
                regionCreation.startingInventoryContent.map { it ?: ItemStack.empty() }
                    .toTypedArray()
            )
            plugin.launch { regionCreation.cancelProtection() }
        }
    }

    companion object {
        /** Small offset used in the world border lerp packet to produce a near-static border animation. */
        private const val BORDER_LERP_OFFSET = 0.001

        @JvmStatic
        fun getProtectionUser(player: OfflinePlayer): ProtectionUser {
            return getProtectionUser(player.uniqueId)
        }

        @JvmStatic
        fun getProtectionUser(uuid: UUID): ProtectionUser {
            return ProtectionUserManager.getProtectionUser(uuid)
        }
    }
}
