package dev.slne.protect.bukkit.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.ProtectionHotbarGui;
import dev.slne.protect.bukkit.listener.listeners.ProtectionModeListener;
import dev.slne.protect.bukkit.listener.listeners.RegionListener;

public class BukkitListenerManager {

    /**
     * Registers all plugin {@link org.bukkit.event.Listener}s
     */
    public void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        JavaPlugin plugin = BukkitMain.getInstance();

        pluginManager.registerEvents(new ProtectionModeListener(), plugin);
        pluginManager.registerEvents(new RegionListener(), plugin);
        pluginManager.registerEvents(new ProtectionHotbarGui(), plugin);
    }

    /**
     * Unregisters all {@link org.bukkit.event.Listener}s
     */
    public void unregisterListeners() {
        HandlerList.unregisterAll(BukkitMain.getInstance());
    }

}
