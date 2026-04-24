package dev.slne.surf.protect.paper.util

import dev.slne.surf.api.paper.util.namespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

private val borderCrossingKey = namespacedKey("border_crossing_messages")

fun Player.hasBorderCrossingMessagesEnabled(): Boolean {
    return persistentDataContainer.get(borderCrossingKey, PersistentDataType.BOOLEAN) ?: true
}

fun Player.setBorderCrossingMessagesEnabled(enabled: Boolean) {
    persistentDataContainer.set(borderCrossingKey, PersistentDataType.BOOLEAN, enabled)
}