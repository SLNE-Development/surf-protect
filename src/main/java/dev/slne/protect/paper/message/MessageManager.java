package dev.slne.protect.paper.message;

import dev.slne.surf.surfapi.core.api.messages.Colors;
import dev.slne.transaction.api.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;

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
   * Returns that the currency could not be found
   *
   * @return The component
   */
  public static Component getNoCurrencyComponent() {
    return prefix().append(Component.text("Die Währung konnte nicht gefunden werden.", ERROR));
  }


  private static void newLine(Builder builder) {
    builder.appendNewline().append(PREFIX);
  }

  /**
   * Returns the component which tells the user that they don't have enough money to rename the
   * protection
   *
   * @return The component
   */
  public static Component getTooExpensiveToRenameComponent() {
    return prefix().append(
        Component.text("Du hast nicht genügend Geld um dieses Grundstück umzubenennen.", ERROR));
  }

  /**
   * Returns the component which tells the user that their protection was created
   *
   * @return The component
   */
  public static Component getProtectionCreatedComponent() {
    return prefix().append(Component.text("Dein Grundstück wurde erfolgreich erstellt.", SUCCESS));
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

  /**
   * Returns the currency display name
   *
   * @return The currency display name
   */
  private static Component currencyDisplayName(Currency currency) {
    return currency.getDisplayName().colorIfAbsent(VARIABLE_VALUE);
  }


  /**
   * Returns the component which tells the user that they have toggled visualization
   *
   * @return the component
   */
  public static Component getProtectionVisualizeComponent(boolean state) {
    return prefix()
        .append(Component.text("Du hast die Visualisierung der Grundstücke ", INFO))
        .append(Component.text(state ? "aktiviert" : "deaktiviert", state ? SUCCESS : ERROR))
        .append(Component.text(". Bitte warte einen kleinen Moment.", INFO));
  }

  public static Component getPlotMessagesChangedComponent(boolean newState) {
    return prefix()
        .append(Component.text("Die Nachrichten beim Betreten/Verlassen eines Grundstücks wurden ",
            INFO))
        .append(Component.text(newState ? "aktiviert" : "deaktiviert", newState ? SUCCESS : ERROR))
        .append(Component.text(".", INFO));
  }

  public static Component getAlreadyProcessingTransactionComponent() {
    return prefix().append(Component.text("Bitte warte einen Moment.", ERROR));
  }
}
