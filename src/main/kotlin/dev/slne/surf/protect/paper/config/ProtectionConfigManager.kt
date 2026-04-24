package dev.slne.surf.protect.paper.config

import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.api.core.config.surfConfigApi
import dev.slne.surf.protect.paper.plugin

class ProtectionConfigManager {
    private val configManager: SpongeConfigManager<ProtectionConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            ProtectionConfig::class.java,
            plugin.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            ProtectionConfig::class.java
        )
        reload()
    }

    fun edit(actions: ProtectionConfig.() -> Unit) {
        configManager.config = configManager.config.apply { actions() }
        configManager.save()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val protectionConfig get() = configManager.config
}