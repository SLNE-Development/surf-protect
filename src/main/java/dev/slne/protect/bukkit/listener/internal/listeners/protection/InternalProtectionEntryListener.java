package dev.slne.protect.bukkit.listener.internal.listeners.protection;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import dev.slne.protect.bukkit.listener.event.ProtectEvent;
import dev.slne.protect.bukkit.listener.event.protection.ProtectionEnterEvent;
import dev.slne.protect.bukkit.listener.event.protection.ProtectionExitEvent;
import dev.slne.protect.bukkit.player.ProtectionPlayer;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;

/**
 * The type Protection enter listener.
 */
public class InternalProtectionEntryListener extends Handler {

  public static final Factory FACTORY = new Factory();

  /**
   * Instantiates a new Protection enter listener.
   *
   * @param session the session
   */
  public InternalProtectionEntryListener(Session session) {
    super(session);
  }

  @Override
  public boolean onCrossBoundary(LocalPlayer player, Location from, Location to,
      ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited,
      MoveType moveType) {

    AtomicBoolean enteredCancelled = new AtomicBoolean(false);
    entered.forEach(region -> {
      ProtectionRegion.getByProtectedRegion(region).forEach(protectionRegion -> {
        enteredCancelled.set(enteredCancelled.get() | callEvent(
            new ProtectionEnterEvent(ProtectionPlayer.get(BukkitAdapter.adapt(player)),
                protectionRegion, BukkitAdapter.adapt(from), BukkitAdapter.adapt(to))));
      });
    });

    AtomicBoolean exitedCancelled = new AtomicBoolean(false);
    exited.forEach(region -> {
      ProtectionRegion.getByProtectedRegion(region).forEach(protectionRegion -> {
        exitedCancelled.set(exitedCancelled.get() | callEvent(
            new ProtectionExitEvent(ProtectionPlayer.get(BukkitAdapter.adapt(player)),
                protectionRegion, BukkitAdapter.adapt(from), BukkitAdapter.adapt(to))));
      });
    });

    return enteredCancelled.get() | exitedCancelled.get();
  }

  /**
   * Call event.
   *
   * @param event the event
   */
  private boolean callEvent(ProtectEvent event) {
    Bukkit.getPluginManager().callEvent(event);

    return event.isCancelled();
  }

  /**
   * The type Factory.
   */
  public static final class Factory extends Handler.Factory<InternalProtectionEntryListener> {

    @Override
    public InternalProtectionEntryListener create(Session session) {
      return new InternalProtectionEntryListener(session);
    }
  }
}
