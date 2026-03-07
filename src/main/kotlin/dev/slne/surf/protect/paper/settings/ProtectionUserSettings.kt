package dev.slne.surf.protect.paper.settings

import dev.slne.surf.protect.paper.plugin
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

enum class ProtectionUserSettings(
    keyName: String,
    private val defaultValue: Boolean
) {
    PLOT_MESSAGES("plot_messages", true);

    private val key: NamespacedKey by lazy { NamespacedKey(plugin, keyName) }

    fun getValue(player: Player): Boolean =
        player.persistentDataContainer.get(key, PersistentDataType.BOOLEAN) ?: defaultValue

    fun toggle(player: Player): Boolean {
        val newValue = !getValue(player)
        player.persistentDataContainer.set(key, PersistentDataType.BOOLEAN, newValue)
        return newValue
    }
}
