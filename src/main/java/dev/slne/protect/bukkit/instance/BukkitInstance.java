package dev.slne.protect.bukkit.instance;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.slne.protect.bukkit.command.BukkitCommandManager;
import dev.slne.protect.bukkit.listener.BukkitListenerManager;
import dev.slne.protect.bukkit.region.flags.ProtectionFlagsRegistry;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerState;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerThread;
import dev.slne.protect.bukkit.user.ProtectionUserManager;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private ProtectionUserManager userManager;
    private ProtectionVisualizerThread protectionVisualizerThread;
    private ProtectionVisualizerState protectionVisualizerState;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();
        userManager = new ProtectionUserManager();

        // Register flags
        final FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT);
        flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT_FLAG);
        flagRegistry.register(ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG);
        flagRegistry.register(ProtectionFlagsRegistry.SURF_BIG_PROTECTION_FLAG);
        flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE);
    }

    /**
     * Called when the plugin is enabled
     */
    public void onEnable() {
        commandManager.registerCommands();

        protectionVisualizerState = new ProtectionVisualizerState();
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

        commandManager.unregisterCommands();
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

    /**
     * @return the commandManager
     */
    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * @return the protectionVisualizerState
     */
    public ProtectionVisualizerState getProtectionVisualizerState() {
        return protectionVisualizerState;
    }
}
