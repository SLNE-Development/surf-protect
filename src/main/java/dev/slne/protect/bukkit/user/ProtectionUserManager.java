package dev.slne.protect.bukkit.user;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.List;
import java.util.UUID;

/**
 * Manages all users
 */
public class ProtectionUserManager {

    private final LoadingCache<UUID, ProtectionUser> users;

    /**
     * Create a new user manager
     */
    public ProtectionUserManager() {
        this.users = Caffeine.newBuilder()
                .build(ProtectionUser::new);
    }

    /**
     * Get all users
     *
     * @return All users
     */
    public List<ProtectionUser> getUsers() {
        return users.asMap().values().stream().toList();
    }

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     * @return The user
     */
    public ProtectionUser getProtectionUser(UUID uuid) {
        return users.get(uuid);
    }
}
