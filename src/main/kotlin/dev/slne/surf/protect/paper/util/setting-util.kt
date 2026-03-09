package dev.slne.surf.protect.paper.util

import dev.slne.surf.surfapi.bukkit.api.util.key
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

private val borderCrossingKey = key("border_crossing_messages")

fun Player.hasBorderCrossingMessagesEnabled(): Boolean {
    return persistentDataContainer.get(borderCrossingKey, PersistentDataType.BOOLEAN) ?: true
}

fun Player.setBorderCrossingMessagesEnabled(enabled: Boolean) {
    persistentDataContainer.set(borderCrossingKey, PersistentDataType.BOOLEAN, enabled)
}