package dev.slne.protect.bukkit.user;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UuidMinecraftNameCache {
    private final List<UuidMinecraftName> uuidMinecraftNames;

    /**
     * Creates a new {@link UuidMinecraftNameCache}
     */
    public UuidMinecraftNameCache() {
        this.uuidMinecraftNames = new ArrayList<>();
    }

    public UuidMinecraftName hitCache(Object uuidOrMinecraftName) {
        return uuidMinecraftNames.stream().filter(uuidMinecraftName -> {
            if (uuidOrMinecraftName instanceof UUID) {
                return uuidMinecraftName.uuid().equals(uuidOrMinecraftName);
            } else if (uuidOrMinecraftName instanceof String) {
                return uuidMinecraftName.minecraftName().equalsIgnoreCase((String) uuidOrMinecraftName);
            }

            return false;
        }).findFirst().orElse(null);
    }

    public UuidMinecraftName setCache(UUID uuid, String minecraftName) {
        UuidMinecraftName cached = hitCache(uuid);

        if (cached != null) {
            uuidMinecraftNames.remove(cached);
        }

        UuidMinecraftName uuidMinecraftName = new UuidMinecraftName(uuid, minecraftName);
        uuidMinecraftNames.add(uuidMinecraftName);

        return uuidMinecraftName;
    }

    public List<UuidMinecraftName> getUuidMinecraftNames() {
        return uuidMinecraftNames;
    }
}
