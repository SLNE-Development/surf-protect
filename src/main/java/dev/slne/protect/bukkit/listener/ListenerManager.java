package dev.slne.protect.bukkit.listener;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.session.SessionManager;
import com.sk89q.worldguard.session.handler.Handler;
import com.sk89q.worldguard.session.handler.Handler.Factory;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.listener.internal.listeners.protection.InternalProtectionEntryListener;
import dev.slne.protect.bukkit.listener.listeners.ProtectionEntryListener;
import dev.slne.protect.bukkit.listener.listeners.ProtectionModeListener;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The type Listener manager.
 */
public class ListenerManager {

  public static final ListenerManager INSTANCE = new ListenerManager();

  private final Set<Handler.Factory<? extends Handler>> worldEditHandlers;
  private final Set<Listener> listeners;

  /**
   * Instantiates a new Listener manager.
   */
  public ListenerManager() {
    this.listeners = new HashSet<>();
    this.worldEditHandlers = new HashSet<>();

    // WorldEdit Events
    registerWorldEditHandler(InternalProtectionEntryListener.FACTORY);

    // Bukkit Events
    registerListener(new ProtectionEntryListener());
    registerListener(new ProtectionModeListener());
  }

  /**
   * Register world edit handlers.
   */
  public void registerWorldEditHandlers() {
    SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();

    worldEditHandlers.forEach(handler -> {
      sessionManager.registerHandler(handler, null);
    });
  }

  /**
   * Unregister world edit handlers.
   */
  public void unregisterWorldEditHandlers() {
    SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();

    worldEditHandlers.forEach(sessionManager::unregisterHandler);
  }

  /**
   * Register listeners.
   */
  public void registerListeners() {
    PluginManager manager = Bukkit.getPluginManager();
    JavaPlugin plugin = BukkitMain.getInstance();

    listeners.forEach(listener -> {
      manager.registerEvents(listener, plugin);
    });
  }

  /**
   * Unregister listeners.
   */
  public void unregisterListeners() {
    listeners.forEach(HandlerList::unregisterAll);
  }

  /**
   * Register listener.
   *
   * @param listener the listener
   */
  public void registerListener(Listener listener) {
    listeners.add(listener);
  }

  /**
   * Unregister listener.
   *
   * @param listener the listener
   */
  public void unregisterListener(Listener listener) {
    listeners.remove(listener);
  }

  /**
   * Register world edit handler.
   *
   * @param handler the handler
   */
  public void registerWorldEditHandler(Factory<? extends Handler> handler) {
    worldEditHandlers.add(handler);
  }

  /**
   * Unregister world edit handler.
   *
   * @param handler the handler
   */
  public void unregisterWorldEditHandler(Factory<? extends Handler> handler) {
    worldEditHandlers.remove(handler);
  }

  /**
   * Gets listeners.
   *
   * @return the listeners
   */
  public Set<Listener> getListeners() {
    return listeners;
  }

  /**
   * Gets world edit handlers.
   *
   * @return the world edit handlers
   */
  public Set<Factory<? extends Handler>> getWorldEditHandlers() {
    return worldEditHandlers;
  }
}
