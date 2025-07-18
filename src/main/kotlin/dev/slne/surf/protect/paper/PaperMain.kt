package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.ShutdownStrategy
import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.mcCoroutineConfiguration
import com.sk89q.worldguard.protection.flags.StateFlag
import dev.slne.protect.paper.metrics.Metrics
import dev.slne.surf.protect.paper.command.CommandManager
import dev.slne.surf.protect.paper.listener.ListenerManager
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    lateinit var metrics: Metrics

    override suspend fun onLoadAsync() {
        metrics = Metrics(this, 26498)
        ProtectionFlagsRegistry.registerFlags()
    }

    override suspend fun onEnableAsync() {
        dev.slne.surf.protect.paper.config.config // Load the configuration
        ListenerManager.registerListeners()
        CommandManager.registerCommands()

        metrics.addCustomChart(Metrics.SingleLineChart("protected_regions") {
            server.worlds.sumOf { world ->
                world.getRegionManagerOrNull()?.regions?.values?.count { region ->
                    region.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) == StateFlag.State.ALLOW
                } ?: 0
            }
        })

        mcCoroutineConfiguration.shutdownStrategy = ShutdownStrategy.MANUAL
    }

    override suspend fun onDisableAsync() {

        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection()
        }

        ListenerManager.unregisterListeners()

        if (::metrics.isInitialized) {
            metrics.shutdown()
        }

        mcCoroutineConfiguration.disposePluginSession()
    }

    companion object {

        /**
         * Returns the instance of the plugin
         *
         * @return The instance of the plugin
         */
        @JvmStatic
        fun getInstance() = plugin
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
