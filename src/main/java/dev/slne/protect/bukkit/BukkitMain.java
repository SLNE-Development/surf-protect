package dev.slne.protect.bukkit;

import java.util.Random;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;

import dev.slne.protect.bukkit.instance.BukkitApi;
import dev.slne.protect.bukkit.instance.BukkitInstance;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

public class BukkitMain extends JavaPlugin {

    private static BukkitMain instance;
    private static BukkitInstance bukkitInstance;

    private Random random;

    @Override
    public void onLoad() {
        instance = this;
        bukkitInstance = new BukkitInstance();

        this.random = new Random();

        BukkitApi.setInstance(bukkitInstance);

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(true);
        PacketEvents.getAPI().load();

        bukkitInstance.onLoad();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        bukkitInstance.onEnable();
    }

    @Override
    public void onDisable() {
        bukkitInstance.onDisable();

        PacketEvents.getAPI().terminate();
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
