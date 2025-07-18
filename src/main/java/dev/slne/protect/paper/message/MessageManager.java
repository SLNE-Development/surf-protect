package dev.slne.protect.paper.message;

import dev.slne.surf.surfapi.core.api.messages.Colors;
import net.kyori.adventure.text.Component;

/**
 * Represents the message manager
 */
@Deprecated(since = "1.0.4")
public final class MessageManager implements Colors {

  /**
   * Utility class
   */
  private MessageManager() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Returns a prefix for the plugin.
   *
   * @return The prefix for the plugin.
   */
  public static Component prefix() {
    return PREFIX;
  }

  /**
   * Returns the component which tells the user that their protection was canceled
   *
   * @return The component
   */
  public static Component getProtectionCanceledComponent() {
    return prefix().append(
        Component.text("Du hast die Erstellung deines Grundstücks abgebrochen.", ERROR));
  }
}
