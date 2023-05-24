package dev.slne.protect.bukkit.user;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Utils for {@link LocalPlayer}s
 */
public class ProtectionUserFinder {

    /**
     * Utility class
     */
    private ProtectionUserFinder() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Find a user by their UUID
     *
     * @param uuid The UUID of the user
     * @return The user
     */
    public static LocalPlayer findLocalPlayer(UUID uuid) {
        if (Bukkit.getPlayer(uuid) != null && Bukkit.getPlayer(uuid).isOnline()) {
            return WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(uuid));
        }

        return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Find a user by their playerName
     *
     * @param playerName The playerName of the user
     * @return The user
     */
    public static LocalPlayer findLocalPlayer(String playerName) {
        if (Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).isOnline()) {
            return WorldGuardPlugin.inst().wrapPlayer(Bukkit.getPlayer(playerName));
        }

        return WorldGuardPlugin.inst().wrapOfflinePlayer(Bukkit.getOfflinePlayer(playerName));
    }

}
