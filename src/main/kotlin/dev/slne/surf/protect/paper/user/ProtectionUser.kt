@file:Suppress("UnstableApiUsage")

package dev.slne.surf.protect.paper.user

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayWorldBorderLerpSize
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sk89q.worldedit.math.BlockVector2
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.config.config
import dev.slne.surf.protect.paper.items.ProtectionItems
import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.protect.paper.region.ProtectionRegion
import dev.slne.surf.protect.paper.util.fastCenter
import dev.slne.surf.protect.paper.util.isInProtectionRegion
import dev.slne.surf.protect.paper.util.standsInProtectedRegion
import dev.slne.surf.protect.paper.util.toLocalPlayer
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.transaction.api.TransactionApi
import dev.slne.transaction.api.currency.Currency
import dev.slne.transaction.api.transaction.data.TransactionData
import dev.slne.transaction.api.transaction.result.TransactionAddResult
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.math.Position
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.util.*
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class ProtectionUser(val uuid: UUID) {

    /** The region that is currently being created, or *null* if none. */
    var regionCreation: ProtectionRegion? = null
        private set

    val isCreatingRegion: Boolean
        get() = this.regionCreation != null


    val localPlayer: LocalPlayer get() = uuid.toLocalPlayer()
    val bukkitPlayer get() = Bukkit.getPlayer(uuid)

    private val protectionModeCooldown = ProtectionCooldownTracker()

    suspend fun addTransaction(
        sender: UUID?,
        amount: BigDecimal,
        currency: Currency,
        data: TransactionData
    ): TransactionAddResult = TransactionApi.getTransactionPlayer(uuid)
        .addTransaction(
            TransactionApi.createTransaction(sender, uuid, currency, amount)
                .apply { setTransactionData(data) }
        )
        .await()

    suspend fun hasEnoughCurrency(amount: BigDecimal, currency: Currency): Boolean =
        TransactionApi.getTransactionPlayer(uuid).hasEnough(currency, amount).await()


    suspend fun startRegionCreation(
        newRegion: ProtectionRegion,
        errorBuilder: (Component) -> Dialog
    ): Boolean {
        val player = this.bukkitPlayer ?: return false

        when {
            isCreatingRegion -> {
                player.showDialog(errorBuilder(buildText { error("Du befindest dich bereits im ProtectionMode.") }))
                return false
            }

            !player.location.isInProtectionRegion() -> {
                player.showDialog(errorBuilder(buildText { error("Du kannst hier keinen ProtectionMode starten.") }))
                return false
            }

            protectionModeCooldown.onCooldown -> {
                val left = protectionModeCooldown.timeLeft.milliseconds
                player.showDialog(errorBuilder(buildText {
                    error("Du kannst den ProtectionMode erst wieder in ")
                    variableValue(left.toString(DurationUnit.SECONDS))
                    error(" verwenden.")
                }))
                return false
            }
        }

        val worldBorder = server.createWorldBorder()
        val (centerPos, size) = computeWorldBorderParams(player, newRegion)

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
            WrapperPlayWorldBorderLerpSize(size, size - 0.001, Long.MAX_VALUE)
        )

        this.regionCreation = newRegion
        return true
    }

    suspend fun resetRegionCreation(aborted: Boolean) {
        val creation = regionCreation ?: return
        this.regionCreation = null

        if (aborted) {
            protectionModeCooldown.recordAbort()
        } else {
            protectionModeCooldown.reset()
        }

        val player = this.bukkitPlayer ?: return
        plugin.launch(plugin.entityDispatcher(player)) {
            restorePlayerProperties(player)
        }

        player.teleportAsync(creation.startLocation).await()
    }

    fun restorePlayerProperties(player: Player) {
        val creation = regionCreation ?: return
        with(player) {
            fallDistance = 0f
            inventory.contents = creation.startingInventoryContent
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

    private fun computeWorldBorderParams(player: Player, region: ProtectionRegion): Pair<Position, Double> {
        val expanding = region.expandingProtection
        return if (expanding != null) {
            val center = expanding.fastCenter().toBlockVector2()
            val size = config.protection.maxDistanceFromStart + maxDistanceFromCenter(expanding, center)
            Position.block(center.x(), 0, center.z()) to size
        } else {
            player.location to config.protection.maxDistanceFromStart
        }
    }

    private fun maxDistanceFromCenter(region: ProtectedRegion, center: BlockVector2): Double {
        val maxSq = region.points.maxOf { it.distanceSq(center) }
        return sqrt(maxSq.toDouble())
    }

    fun sendMessage(message: Component) {
        this.bukkitPlayer?.sendMessage(message)
    }

    companion object {
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
