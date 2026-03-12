package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import dev.slne.surf.protect.paper.command.CommandManager
import dev.slne.surf.protect.paper.config.ProtectionConfigManager
import dev.slne.surf.protect.paper.listener.ListenerManager
import dev.slne.surf.protect.paper.menu.view.ProtectionInfoView
import dev.slne.surf.protect.paper.menu.view.ProtectionMainView
import dev.slne.surf.protect.paper.menu.view.flags.ProtectionEditFlagsView
import dev.slne.surf.protect.paper.menu.view.list.ProtectionListView
import dev.slne.surf.protect.paper.menu.view.members.ProtectionMemberListView
import dev.slne.surf.protect.paper.menu.view.members.ProtectionMemberRemoveConfirmView
import dev.slne.surf.protect.paper.menu.view.sell.ProtectionSellConfirmView
import dev.slne.surf.protect.paper.papi.PapiExpansion
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import dev.slne.surf.surfapi.bukkit.api.hook.papi.papiHook
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.bukkit.api.util.chunkX
import dev.slne.surf.surfapi.bukkit.api.util.chunkZ
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    override suspend fun onLoadAsync() {
        ProtectionFlagsRegistry.registerFlags()

        viewFrame.with(ProtectionEditFlagsView)
        viewFrame.with(ProtectionListView)
        viewFrame.with(ProtectionMemberListView)
        viewFrame.with(ProtectionMemberRemoveConfirmView)
        viewFrame.with(ProtectionSellConfirmView)
        viewFrame.with(ProtectionInfoView)
        viewFrame.with(ProtectionMainView)
    }

    override suspend fun onEnableAsync() {
        ListenerManager.registerListeners()
        CommandManager.registerCommands()

        coroutineScope {
            launch {
                restoreMarkers()
            }
        }

        papiHook.register(PapiExpansion)
    }

    override suspend fun onDisableAsync() {
        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection(true)
        }

        ListenerManager.unregisterListeners()
    }

    private suspend fun restoreMarkers() {
        if (configManager.protectionConfig.dirtyMarkers.isNotEmpty()) {
            plugin.logger.info("Restoring ${configManager.protectionConfig.dirtyMarkers.size} dirty markers...")
        }

        configManager.protectionConfig.dirtyMarkers.forEach { marker ->
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
        }

        if (configManager.protectionConfig.dirtyMarkers.isNotEmpty()) {
            plugin.logger.info("Finished restoring dirty markers.")
        }
    }
}

val configManager = ProtectionConfigManager()
val config get() = configManager.protectionConfig

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
