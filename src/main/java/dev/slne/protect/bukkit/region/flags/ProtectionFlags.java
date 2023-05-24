package dev.slne.protect.bukkit.region.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * Represents the protection flags
 */
public class ProtectionFlags {

    /**
     * This flag is used to determine if a region is able to be protected or not
     */
    public static final StateFlag SURVIVAL_PROTECT = new StateFlag("can-surf-protect", false);

    /**
     * This flag is used to determine if a region is able to be sold or not
     */
    public static final StateFlag SURVIVAL_CAN_SELL_FLAG = new StateFlag("can-surf-protect-sell", true);

    /**
     * This flag contains the protection flag info
     */
    public static final ProtectionFlag SURVIVAL_PROTECT_FLAG = new ProtectionFlag("surf-protect-info");

    private ProtectionFlags() {
        throw new IllegalStateException("Utility class");
    }
}
