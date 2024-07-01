package dev.slne.protect.bukkit.listener.event;

import java.time.ZonedDateTime;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The type protect event.
 */
public class ProtectEvent extends Event implements Cancellable {

  private static final HandlerList HANDLER_LIST = new HandlerList();
  private boolean cancel;

  private final ZonedDateTime timestamp;

  /**
   * Instantiates a new protect event.
   */
  public ProtectEvent() {
    this(false);
  }

  /**
   * Instantiates a new protect event.
   *
   * @param async the async
   */
  public ProtectEvent(boolean async) {
    super(async);

    this.timestamp = ZonedDateTime.now();
  }

  /**
   * Gets timestamp.
   *
   * @return the timestamp
   */
  public ZonedDateTime getTimestamp() {
    return timestamp;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  /**
   * Gets handler list.
   *
   * @return the handler list
   */
  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  @Override
  public boolean isCancelled() {
    return cancel;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancel = cancel;
  }
}
