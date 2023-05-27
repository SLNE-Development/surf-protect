package dev.slne.protect.bukkit.instance;

import com.sk89q.worldguard.WorldGuard;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.command.BukkitCommandManager;
import dev.slne.protect.bukkit.listener.BukkitListenerManager;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.visual.visualizer.VisualizerTask;
import dev.slne.protect.bukkit.user.ProtectionUserManager;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private ProtectionUserManager userManager;
    private VisualizerTask visualizerTask;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(BukkitMain.getInstance()));
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();
        userManager = new ProtectionUserManager();

        // Register flags
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURVIVAL_PROTECT);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURVIVAL_PROTECT_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURVIVAL_CAN_SELL_FLAG);
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        CommandAPI.onEnable();
        commandManager.registerCommands();

        listenerManager.registerListeners();

        visualizerTask = new VisualizerTask();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        listenerManager.unregisterListeners();
        CommandAPI.onDisable();
    }

    /**
     * Returns the {@link BukkitListenerManager}
     *
     * @return the {@link BukkitListenerManager}
     */
    public BukkitListenerManager getListenerManager() {
        return listenerManager;
    }

    /**
     * Returns the {@link ProtectionUserManager}
     *
     * @return the {@link ProtectionUserManager}
     */
    public ProtectionUserManager getUserManager() {
        return userManager;
    }

    /**
     * Returns the {@link VisualizerTask}
     *
     * @return the {@link VisualizerTask}
     */
    public VisualizerTask getVisualizerTask() {
        return visualizerTask;
    }

}
