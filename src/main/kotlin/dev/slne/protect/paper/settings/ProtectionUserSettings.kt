package dev.slne.protect.paper.settings

import dev.slne.surf.surfapi.bukkit.api.util.key
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.persistence.PersistentDataViewHolder
import net.kyori.adventure.text.Component
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

enum class ProtectionUserSettings(val id: String, val displayName: Component, val defaultState: Boolean) {
    PLOT_MESSAGES("plot_messages", text("Grundstück Nachrichten", Colors.Companion.PRIMARY), true);

    val key = key("settings-$id")

    fun getValue(holder: PersistentDataViewHolder) = getValue(holder.persistentDataContainer)
    fun getValue(view: PersistentDataContainerView): Boolean =
        view.getOrDefault(key, PersistentDataType.BOOLEAN, defaultState)

    fun setValue(holder: PersistentDataHolder, value: Boolean) {
        setValue(holder.persistentDataContainer, value)
    }

    fun setValue(pdc: PersistentDataContainer, value: Boolean) {
        pdc.set(key, PersistentDataType.BOOLEAN, value)
    }
}