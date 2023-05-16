package dev.slne.protect.bukkit.instance;

import com.sk89q.worldguard.WorldGuard;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.slne.data.bukkit.BukkitDataSource;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.command.BukkitCommandManager;
import dev.slne.protect.bukkit.listener.BukkitListenerManager;
import dev.slne.protect.bukkit.user.ProtectionUserManager;
import dev.slne.protect.bukkit.utils.ProtectionSettings;

public class BukkitInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private BukkitDataSource dataSource;

    private ProtectionUserManager userManager;

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(BukkitMain.getInstance()));
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();

        dataSource = new BukkitDataSource(BukkitMain.getInstance());
        dataSource.onLoad();

        userManager = new ProtectionUserManager();

        WorldGuard.getInstance().getFlagRegistry().register(ProtectionSettings.SURVIVAL_PROTECT);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionSettings.SURVIVAL_PROTECT_FLAG);
        WorldGuard.getInstance().getFlagRegistry().register(ProtectionSettings.SURVIVAL_CAN_SELL_FLAG);
    }

    public void onEnable() {
        CommandAPI.onEnable();
        commandManager.registerCommands();

        listenerManager.registerListeners();

        dataSource.onEnable();
    }

    public void onDisable() {
        listenerManager.unregisterListeners();
        dataSource.onDisable();
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
     * Returns the {@link BukkitDataSource}
     * 
     * @return the {@link BukkitDataSource}
     */
    public BukkitDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Returns the {@link ProtectionUserManager}
     * 
     * @return the {@link ProtectionUserManager}
     */
    public ProtectionUserManager getUserManager() {
        return userManager;
    }

}
