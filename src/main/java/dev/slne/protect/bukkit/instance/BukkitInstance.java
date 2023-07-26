package dev.slne.protect.bukkit.instance;

import com.sk89q.worldguard.WorldGuard;
import dev.slne.protect.bukkit.command.BukkitCommandManager;
import dev.slne.protect.bukkit.listener.BukkitListenerManager;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerState;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizerThread;
import dev.slne.protect.bukkit.user.ProtectionUserManager;
import dev.slne.protect.bukkit.user.UuidMinecraftNameCache;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private ProtectionUserManager userManager;
    private ProtectionVisualizerThread protectionVisualizerThread;
    private ProtectionVisualizerState protectionVisualizerState;
    private UuidMinecraftNameCache uuidMinecraftNameCache;

    /**
     * Called when the plugin is loaded
     */
    public void onLoad() {
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();
        userManager = new ProtectionUserManager();

        this.uuidMinecraftNameCache = new UuidMinecraftNameCache();

        // Register flags
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_PROTECT);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_PROTECT_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_CAN_SELL_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_BIG_PROTECTION_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionFlags.SURF_PROTECT_VISUALIZE);
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

    /**
     * Returns the {@link UuidMinecraftNameCache}
     *
     * @return the {@link UuidMinecraftNameCache}
     */
    public UuidMinecraftNameCache getUuidMinecraftNameCache() {
        return uuidMinecraftNameCache;
    }
}
