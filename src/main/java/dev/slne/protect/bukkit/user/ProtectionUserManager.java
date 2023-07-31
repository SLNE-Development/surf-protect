package dev.slne.protect.bukkit.user;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages all users
 */
public class ProtectionUserManager {

    private final List<ProtectionUser> users;

    /**
     * Create a new user manager
     */
    public ProtectionUserManager() {
        this.users = new ArrayList<>();
    }

    /**
     * Get all users
     *
     * @return All users
     */
    public List<ProtectionUser> getUsers() {
        return users;
    }

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     *
     * @return The user
     */
    public ProtectionUser getProtectionUser(UUID uuid) {
        return this.users.stream().filter(user -> user.getUuid().equals(uuid)).findFirst().orElseGet(() -> {
            ProtectionUser user = new ProtectionUser(uuid);
            users.add(user);

            return user;
        });
    }

}
