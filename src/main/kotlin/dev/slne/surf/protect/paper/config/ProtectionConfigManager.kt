package dev.slne.surf.protect.paper.config

import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi

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