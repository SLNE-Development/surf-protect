package dev.slne.protect.bukkit.region.flag;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.NamespacedKey;

/**
 * The type Protection flags registry.
 */
public class ProtectionFlagsRegistry {

  public static final ProtectionFlagsRegistry INSTANCE = new ProtectionFlagsRegistry();

  /**
   * The key for the plot messages
   */
  public static final NamespacedKey PLOT_MESSAGES_KEY = new NamespacedKey("protect",
      "plot_messages");

  /**
   * The default value for the plot messages
   */
  public static final boolean DEFAULT_PLOT_MESSAGES = true;

  /**
   * This flag is used to determine if a region is able to be protected or not
   */
  public static StateFlag CAN_SURF_PROTECT = new StateFlag("can-surf-protect", false);

  /**
   * This flag is used to determine if a region is able to be sold or not
   */
  public static StateFlag CAN_SURF_PROTECT_SELL = new StateFlag("can-surf-protect-sell", true);

  /**
   * This flag is used to determine if a region is a Großgrundstück or not
   */
  public static StateFlag SURF_PROTECT_BIG_PROJECT = new StateFlag("surf-protect-big", false);

  /**
   * This flag contains the protection flag info
   */
  public static ProtectionFlag SURF_PROTECT_INFO = new ProtectionFlag("surf-protect-info");

  /**
   * This flag is used to determine if a region was protected using the protection system
   */
  public static StateFlag SURF_PROTECT_IS_PROTECTION = new StateFlag("surf-protection", false);

  /**
   * Register flags.
   */
  public void registerFlags() {
    final FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();

    flagRegistry.register(ProtectionFlagsRegistry.CAN_SURF_PROTECT);
    flagRegistry.register(ProtectionFlagsRegistry.CAN_SURF_PROTECT_SELL);
    flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT_BIG_PROJECT);
    flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT_INFO);
    flagRegistry.register(ProtectionFlagsRegistry.SURF_PROTECT_IS_PROTECTION);
  }

}
