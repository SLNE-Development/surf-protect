package dev.slne.surf.protect.paper.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.hours

object ProtectionUserManager {
    private val users = Caffeine.newBuilder()
        .expireAfterAccess(1.hours)
        .build<UUID, ProtectionUser> { ProtectionUser(it) }

    fun all() = users.asMap().values
    fun getProtectionUser(uuid: UUID): ProtectionUser = users.get(uuid)
}

fun Player.protectionUser(): ProtectionUser {
    return ProtectionUserManager.getProtectionUser(this.uniqueId)
}