package dev.slne.surf.protect.paper.region.flags

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.StateFlag

object ProtectionFlagsRegistry {
    /**
     * This flag is used to determine if a region is able to be protected or not
     */
    @JvmField
    val SURF_PROTECT = StateFlag("can-surf-protect", false)

    /**
     * This flag is used to determine if a region is able to be sold or not
     */
    @JvmField
    val SURF_CAN_SELL_FLAG = StateFlag("can-surf-protect-sell", true)

    /**
     * This flag is used to determine if a region is a Großgrundstück or not
     */
    @JvmField
    val SURF_BIG_PROTECTION_FLAG = StateFlag("surf-protect-big", false)

    /**
     * This flag contains the protection flag info
     */
    @JvmField
    val SURF_PROTECT_FLAG = ProtectionFlag("surf-protect-info")

    /**
     * This flag contains the protection flag info
     */
    @JvmField
    val SURF_PROTECT_VISUALIZE = StateFlag("surf-protect-visualize", true)

    /**
     * This flag is used to determine if a region was protected using the protection system
     */
    @JvmField
    val SURF_PROTECTION = StateFlag("surf-protection", false)

    fun registerFlags() = with(WorldGuard.getInstance().flagRegistry) {
        register(SURF_PROTECT)
        register(SURF_PROTECT_FLAG)
        register(SURF_CAN_SELL_FLAG)
        register(SURF_BIG_PROTECTION_FLAG)
        register(SURF_PROTECT_VISUALIZE)
        register(SURF_PROTECTION)
    }
}
