package dev.slne.protect.bukkit;

import dev.slne.protect.bukkit.instance.BukkitApi;
import dev.slne.protect.bukkit.instance.BukkitInstance;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class BukkitMain extends JavaPlugin {

    private static BukkitMain instance;
    private static BukkitInstance bukkitInstance;

    private Random random;

    @Override
    @SuppressWarnings("java:S2696")
    public void onLoad() {
        instance = this;
        bukkitInstance = new BukkitInstance();

        this.random = new Random();

        BukkitApi.setInstance(bukkitInstance);

        bukkitInstance.onLoad();
    }

    @Override
    public void onEnable() {
        bukkitInstance.onEnable();
    }

    @Override
    public void onDisable() {
        bukkitInstance.onDisable();
    }

    /**
     * Returns the instance of the plugin
     *
     * @return The instance of the plugin
     */
    public static BukkitMain getInstance() {
        return instance;
    }

    /**
     * Returns the core instance of the plugin
     *
     * @return The core instance of the plugin
     */
    public static BukkitInstance getBukkitInstance() {
        return bukkitInstance;
    }

    /**
     * Returns the random instance
     *
     * @return The random instance
     */
    public static Random getRandom() {
        return instance.random;
    }

}
