package dev.slne.surf.protect.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
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

        papiHook.register(PapiExpansion)
    }

    override suspend fun onDisableAsync() {
        ProtectionUserManager.all().forEach { user ->
            user.regionCreation?.cancelProtection()
        }

        ListenerManager.unregisterListeners()
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

val configManager = ProtectionConfigManager()
val config get() = configManager.protectionConfig

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)
