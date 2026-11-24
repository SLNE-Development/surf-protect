package dev.slne.surf.protect.paper.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.protect.paper.plugin
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey

class StructureSurchargeConfigManager {
    private val configManager: SpongeConfigManager<StructureSurchargeConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            StructureSurchargeConfig::class.java,
            plugin.dataPath,
            "structure-surcharges.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            StructureSurchargeConfig::class.java
        )
        reload()
    }

    /**
     * Reloads the config from file and adds every structure from the registry that is
     * not yet listed with a surcharge of 0. Existing entries are never removed or changed.
     */
    fun reload() {
        configManager.reloadFromFile()
        syncWithRegistry()
    }

    private fun syncWithRegistry() {
        val current = configManager.config.surcharges
        val registryKeys = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.STRUCTURE)
            .keyStream()
            .map { it.asString() }
            .toList()

        val missing = registryKeys.filterNot { it in current }
        if (missing.isEmpty()) return

        val merged = LinkedHashMap<String, Double>(current.size + missing.size)
        (current.keys + missing).sorted().forEach { key ->
            merged[key] = current[key] ?: 0.0
        }

        configManager.config = configManager.config.copy(surcharges = merged)
        configManager.save()

        plugin.logger.info("Added ${missing.size} new structures to structure-surcharges.yml")
    }

    val structureSurchargeConfig get() = configManager.config
}
