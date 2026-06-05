package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.surf.api.paper.api.metrics.Metrics
import dev.slne.surf.api.paper.hook.papi.SurfPaperPAPIHook
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.api.paper.util.chunkX
import dev.slne.surf.api.paper.util.chunkZ
import dev.slne.surf.protect.paper.command.CommandManager
import dev.slne.surf.protect.paper.config.ProtectionConfigManager
import dev.slne.surf.protect.paper.listener.ListenerManager
import dev.slne.surf.protect.paper.menu.view.flags.protectionEditFlagsView
import dev.slne.surf.protect.paper.menu.view.list.protectionListView
import dev.slne.surf.protect.paper.menu.view.members.protectionMemberListView
import dev.slne.surf.protect.paper.menu.view.protectionInfoView
import dev.slne.surf.protect.paper.menu.view.protectionMainView
import dev.slne.surf.protect.paper.papi.PapiExpansion
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    lateinit var metrics: Metrics

    override suspend fun onLoadAsync() {
        metrics = Metrics(this, 26498)
        ProtectionFlagsRegistry.registerFlags()

        protectionMainView.register()
        protectionInfoView.register()
        protectionEditFlagsView.register()
        protectionListView.register()
        protectionMemberListView.register()
    }

    override suspend fun onEnableAsync() {
        ListenerManager.registerListeners()
        CommandManager.registerCommands()

        plugin.launch {
            restoreMarkers()
        }

        SurfPaperPAPIHook.register(PapiExpansion)

        metrics.addCustomChart(Metrics.SingleLineChart("protected_regions") {
            server.worlds.sumOf { world ->
                world.getRegionManagerOrNull()?.regions?.values?.count { region ->
                    region.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) == StateFlag.State.ALLOW
                } ?: 0
            }
        })
    }

    override suspend fun onDisableAsync() {
        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection(true)
        }

        ListenerManager.unregisterListeners()

        if (::metrics.isInitialized) {
            metrics.shutdown()
        }
    }

    private suspend fun restoreMarkers() {
        val markersToRestore = configManager.protectionConfig.dirtyMarkers.toList()
        if (markersToRestore.isNotEmpty()) {
            plugin.logger.info("Restoring ${markersToRestore.size} dirty markers...")
        }

        for (marker in markersToRestore) {
            val chunkX = marker.location.chunkX
            val chunkZ = marker.location.chunkZ
            val world = marker.location.world

            val chunkBlockX = marker.location.blockX and 15
            val chunkBlockZ = marker.location.blockZ and 15

            val chunk = world.getChunkAtAsync(chunkX, chunkZ).await()
            withContext(plugin.regionDispatcher(world, chunk.x, chunk.z)) {
                chunk.getBlock(chunkBlockX, marker.location.blockY, chunkBlockZ).blockData =
                    marker.blockData
            }

            configManager.edit {
                dirtyMarkers.remove(marker)
            }
        }

        if (configManager.protectionConfig.dirtyMarkers.isNotEmpty()) {
            plugin.logger.info("Finished restoring dirty markers.")
        }
    }
}

val configManager = ProtectionConfigManager()
val config get() = configManager.protectionConfig

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
