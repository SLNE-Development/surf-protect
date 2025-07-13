package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.protect.paper.metrics.Metrics
import dev.slne.surf.protect.paper.command.CommandManager
import dev.slne.surf.protect.paper.listener.ListenerManager
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    lateinit var metrics: Metrics

    override suspend fun onLoadAsync() {
        metrics = Metrics(this, 26498)
        ProtectionFlagsRegistry.registerFlags()
    }

    override suspend fun onEnableAsync() {
        config // Load the configuration
        ListenerManager.registerListeners()
        CommandManager.registerCommands()
    }

    override suspend fun onDisableAsync() {

        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection()
        }

        ListenerManager.unregisterListeners()

        if (::metrics.isInitialized) {
            metrics.shutdown()
        }
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
