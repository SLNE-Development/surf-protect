package dev.slne.protect.bukkit;

import dev.slne.protect.bukkit.listener.ListenerManager;
import dev.slne.protect.bukkit.region.flag.ProtectionFlagsRegistry;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The type Bukkit main.
 */
public class BukkitMain extends JavaPlugin {

  private ProtectionFlagsRegistry flagsRegistry;
  private ListenerManager listenerManager;

  @Override
  public void onLoad() {
    listenerManager = ListenerManager.INSTANCE;

    flagsRegistry = ProtectionFlagsRegistry.INSTANCE;
    flagsRegistry.registerFlags();
  }

  @Override
  public void onEnable() {
    listenerManager.registerWorldEditHandlers();
    listenerManager.registerListeners();
  }

  @Override
  public void onDisable() {
    listenerManager.unregisterWorldEditHandlers();
    listenerManager.unregisterListeners();
  }

  /**
   * Returns the plugin instance
   *
   * @return the plugin instance, which is collected by using the getPlugin Method in
   * {@link JavaPlugin#getPlugin}
   */
  public static BukkitMain getInstance() {
    return getPlugin(BukkitMain.class);
  }

  /**
   * Gets flags registry.
   *
   * @return the flags registry
   */
  public ProtectionFlagsRegistry getFlagsRegistry() {
    return flagsRegistry;
  }

  /**
   * Gets listener manager.
   *
   * @return the listener manager
   */
  public ListenerManager getListenerManager() {
    return listenerManager;
  }
}
