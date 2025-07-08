package dev.slne.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.protect.paper.instance.BukkitApi
import dev.slne.protect.paper.instance.BukkitInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {

    override suspend fun onLoadAsync() {
        bukkitInstance = BukkitInstance()
        BukkitApi.setInstance(bukkitInstance)

        bukkitInstance.onLoad()
    }

    override suspend fun onEnableAsync() {
        bukkitInstance.onEnable()

    }

    override suspend fun onDisableAsync() {
        bukkitInstance.onDisable()

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
