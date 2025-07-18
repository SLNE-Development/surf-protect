package dev.slne.surf.protect.paper.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.protect.paper.plugin
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.hours

object ProtectionUserManager {
    private val users = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .maximumSize(3000)
        .removalListener<UUID, ProtectionUser> { uuid, user, cause ->
            if (cause.wasEvicted() && user != null) {
                val player = user.bukkitPlayer ?: return@removalListener
                plugin.launch(plugin.entityDispatcher(player)) {
                    user.handleQuit(player)
                }
            }
        }
        .build<UUID, ProtectionUser> { ProtectionUser(it) }

    fun all() = users.asMap().values
    fun getProtectionUser(uuid: UUID): ProtectionUser = users.get(uuid)
}

fun Player.protectionUser(): ProtectionUser {
    return ProtectionUserManager.getProtectionUser(this.uniqueId)
}