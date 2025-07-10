package dev.slne.surf.protect.paper.config

import dev.slne.surf.protect.paper.plugin
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

val config by lazy {
    surfConfigApi.createSpongeYmlConfig<ProtectionConfig>(plugin.dataPath, "config.yml")
}

@ConfigSerializable
data class ProtectionConfig(
    val worldAllowList: List<String> = listOf(server.worlds.first().name),

    @param:Comment("The max distance a user is allowed to go from the protection start before being thrown back")
    val maxDistanceFromProtectionStart: Double = 100.0,

    val protectionModeBaseCooldownMs: Long = 300000, // 5 minutes
    val protectionModeMaxCooldownMs: Long = 3600000, // 1 hour

    val minYWorld: Int = -64,
    val maxYWorld: Int = 319,

    val areaMinBlocks: Long = 250,
    val areaMaxBlocks: Long = Int.MAX_VALUE.toLong(),
) {
}