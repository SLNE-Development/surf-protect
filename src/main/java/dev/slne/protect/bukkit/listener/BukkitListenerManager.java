package dev.slne.protect.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.slne.protect.bukkit.PaperMain;
import dev.slne.protect.bukkit.listener.listeners.ProtectionHotbarListener;
import dev.slne.protect.bukkit.listener.listeners.ProtectionModeListener;
import dev.slne.protect.bukkit.listener.listeners.RegionListener;

public class BukkitListenerManager {

    /**
     * Registers all plugin {@link org.bukkit.event.Listener}s
     */
    public void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        JavaPlugin plugin = PaperMain.getInstance();

        pluginManager.registerEvents(new ProtectionModeListener(), plugin);
        pluginManager.registerEvents(new ProtectionHotbarListener(), plugin);
        pluginManager.registerEvents(new RegionListener(), plugin);
    }

    /**
     * Unregister all {@link org.bukkit.event.Listener}s
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(PaperMain.getInstance());
    }

}
