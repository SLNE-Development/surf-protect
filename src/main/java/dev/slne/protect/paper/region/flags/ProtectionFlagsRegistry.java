package dev.slne.protect.paper.region.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * Represents the protection flags
 */
public interface ProtectionFlagsRegistry {

    /**
     * This flag is used to determine if a region is able to be protected or not
     */
    StateFlag SURF_PROTECT = new StateFlag("can-surf-protect", false);

    /**
     * This flag is used to determine if a region is able to be sold or not
     */
    StateFlag SURF_CAN_SELL_FLAG = new StateFlag("can-surf-protect-sell", true);

    /**
     * This flag is used to determine if a region is a Großgrundstück or not
     */
    StateFlag SURF_BIG_PROTECTION_FLAG = new StateFlag("surf-protect-big", false);

    /**
     * This flag contains the protection flag info
     */
    ProtectionFlag SURF_PROTECT_FLAG = new ProtectionFlag("surf-protect-info");

    /**
     * This flag contains the protection flag info
     */
    StateFlag SURF_PROTECT_VISUALIZE = new StateFlag("surf-protect-visualize", true);

    /**
     * This flag is used to determine if a region was protected using the protection system
     */
    StateFlag SURF_PROTECTION = new StateFlag("surf-protection", false);
}
