package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.protect.paper.instance.BukkitInstance
import dev.slne.surf.protect.paper.command.CommandManager
import dev.slne.surf.protect.paper.listener.ListenerManager
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.user.ProtectionUserManager
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    override suspend fun onLoadAsync() {
        bukkitInstance = BukkitInstance()

        ProtectionFlagsRegistry.registerFlags()
    }

    override suspend fun onEnableAsync() {
        bukkitInstance.onEnable()
        config // Load the configuration
        ListenerManager.registerListeners()
        CommandManager.registerCommands()
    }

    override suspend fun onDisableAsync() {
        bukkitInstance.onDisable()

        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection()
        }

        ListenerManager.unregisterListeners()
    }

    companion object {
        private lateinit var bukkitInstance: BukkitInstance

        /**
         * Returns the instance of the plugin
         *
         * @return The instance of the plugin
         */
        @JvmStatic
        fun getInstance() = plugin

        /**
         * Returns the core instance of the plugin
         *
         * @return The core instance of the plugin
         */
        @JvmStatic
        fun getBukkitInstance(): BukkitInstance {
            return bukkitInstance
        }
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
val bukkitInstance get() = PaperMain.getBukkitInstance()
