package dev.slne.protect.bukkit.region.visual.visualizer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.slne.protect.bukkit.BukkitMain;

public class ProtectionVisualizerState implements Listener {

    private Map<UUID, Boolean> playerState;

    /**
     * Construct a new protection visualizer state
     */
    public ProtectionVisualizerState() {
        this.playerState = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, BukkitMain.getInstance());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerState.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Returns the state of the player
     *
     * @param uuid the uuid of the player
     * @return the state of the player
     */
    public boolean getPlayerState(UUID uuid) {
        return playerState.getOrDefault(uuid, false);
    }

    /**
     * Returns the state of the player
     *
     * @param player the player
     * @return the state of the player
     */
    public boolean getPlayerState(Player player) {
        return getPlayerState(player.getUniqueId());
    }

    /**
     * Sets the state of the player
     *
     * @param uuid  the uuid of the player
     * @param state the state of the player
     */
    public void setPlayerState(UUID uuid, boolean state) {
        playerState.put(uuid, state);
    }

    /**
     * Sets the state of the player
     *
     * @param player the player
     * @param state  the state of the player
     */
    public void setPlayerState(Player player, boolean state) {
        setPlayerState(player.getUniqueId(), state);
    }

    /**
     * Toggles the state of the player
     *
     * @param uuid the uuid of the player
     */
    public void togglePlayerState(UUID uuid) {
        setPlayerState(uuid, !getPlayerState(uuid));
    }

    /**
     * Toggles the state of the player
     *
     * @param player the player
     */
    public void togglePlayerState(Player player) {
        togglePlayerState(player.getUniqueId());
    }

    /**
     * @return the playerState
     */
    public Map<UUID, Boolean> getPlayerState() {
        return playerState;
    }

}
