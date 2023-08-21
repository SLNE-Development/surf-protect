package dev.slne.protect.bukkit.region.info;

/**
 * A ProtectionFlagInfo is used on the actual WorldGuard region to store
 * attributes like the name and other values
 *
 * @param name The name of the protection
 */
public record ProtectionFlagInfo(String name) {

    /**
     * Returns a copy of this protection info with a new name
     *
     * @param name The new name
     *
     * @return The copy
     */
    public ProtectionFlagInfo copyWithNewName(String name) {
        return new ProtectionFlagInfo(name);
    }
}
