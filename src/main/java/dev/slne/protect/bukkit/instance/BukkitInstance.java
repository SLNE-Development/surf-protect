package dev.slne.protect.bukkit.instance;

import com.sk89q.worldguard.WorldGuard;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.command.BukkitCommandManager;
import dev.slne.protect.bukkit.listener.BukkitListenerManager;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerThread;
import dev.slne.protect.bukkit.user.ProtectionUserManager;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private ProtectionUserManager userManager;
    private ProtectionVisualizerThread protectionVisualizerThread;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(BukkitMain.getInstance()));
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();
        userManager = new ProtectionUserManager();

        // Register flags
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_PROTECT);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_PROTECT_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_CAN_SELL_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_BIG_PROTECTION_FLAG);
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        CommandAPI.onEnable();
        commandManager.registerCommands();

        listenerManager.registerListeners();

        protectionVisualizerThread = new ProtectionVisualizerThread();
        protectionVisualizerThread.start();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        protectionVisualizerThread.stop();
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
     * Returns the {@link ProtectionVisualizerThread}
     *
     * @return the {@link ProtectionVisualizerThread}
     */
    public ProtectionVisualizerThread getProtectionVisualizerThread() {
        return protectionVisualizerThread;
    }

}
