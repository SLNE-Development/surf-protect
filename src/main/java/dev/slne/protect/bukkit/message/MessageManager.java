package dev.slne.protect.bukkit.message;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.Profile;
import dev.slne.protect.bukkit.region.flag.info.ProtectionFlagInfo;
import dev.slne.protect.bukkitold.region.info.RegionInfo;
import dev.slne.protect.bukkitold.region.settings.ProtectionSettings;
import dev.slne.protect.bukkitold.user.ProtectionUser;
import dev.slne.surf.surfapi.core.api.messages.Colors;
import dev.slne.transaction.api.currency.Currency;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the message manager
 */
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

  /**
   * Returns a component which tells the user that they need to place more markers
   *
   * @param placedMarkers The amount of placed markers
   * @return The component
   */
  public static Component getMoreMarkersComponent(int placedMarkers) {
    int neededMarkers = ProtectionSettings.MIN_MARKERS - placedMarkers;

    return prefix()
        .append(Component.text("Du musst mindestens ", ERROR))
        .append(Component.text(neededMarkers, VARIABLE_VALUE))
        .append(
            Component.text(" weitere%s Marker platzieren.".formatted(neededMarkers == 1 ? "n" : ""),
                ERROR));
  }

  /**
   * Returns a component which tells the user that the region overlaps with another region
   *
   * @return The component
   */
  public static Component getOverlappingRegionsComponent() {
    return prefix().append(
        Component.text("Die markierte Fläche kollidiert mit einem anderen Grundstück.", ERROR));
  }

  /**
   * Returns a component which tells the user that the region is too small
   *
   * @return The component
   */
  public static Component getAreaTooSmallComponent() {
    return prefix().append(Component.text("Die markierte Fläche ist zu klein.", ERROR));
  }

  /**
   * Returns a component which tells the user that the region is too big
   *
   * @return The component
   */
  public static Component getAreaTooBigComponent() {
    return prefix().append(Component.text("Die markierte Fläche ist zu groß.", ERROR));
  }

  /**
   * New line.
   *
   * @param builder the builder
   */
  private static void newLine(Builder builder) {
    builder.appendNewline().append(PREFIX);
  }

  /**
   * Sends the area buy header to the user
   *
   * @param area            The area
   * @param effectiveCost   The effective cost
   * @param currency        The currency
   * @param pricePerBlock   The price per block
   * @param distanceToSpawn The distance to spawn
   * @return the builder
   */
  private static Builder buildAreaBuyHeader(long area, double effectiveCost, Currency currency,
      double pricePerBlock, double distanceToSpawn) {

    distanceToSpawn = Math.round(distanceToSpawn * 100.0) / 100.0;
    final double roundedEffectiveCost = Math.round(effectiveCost * 100.0) / 100.0;

    final Builder builder = Component.text();

    builder.append(PREFIX);
    newLine(builder);

    builder.append(Component.text("Das Grundstück steht zum Verkauf", SUCCESS));
    newLine(builder);
    newLine(builder);

    builder.append(Component.text("Fläche: ", VARIABLE_KEY));
    builder.append(Component.text(area, VARIABLE_VALUE));
    builder.append(Component.text(" Blöcke", VARIABLE_VALUE));
    newLine(builder);

    builder.append(Component.text("Preis/Block: ", VARIABLE_KEY));
    builder.append(Component.text(pricePerBlock, VARIABLE_VALUE));
    builder.appendSpace();
    builder.append(currencyDisplayName(currency));
    newLine(builder);

    builder.append(Component.text("Distanz zum Spawn: ", VARIABLE_KEY));
    builder.append(Component.text(distanceToSpawn, VARIABLE_VALUE));
    builder.append(Component.text(" Blöcke", VARIABLE_VALUE));
    newLine(builder);

    builder.append(Component.text("Kaufpreis: ", VARIABLE_KEY));
    builder.append(Component.text(roundedEffectiveCost, VARIABLE_VALUE));
    builder.appendSpace();
    builder.append(currencyDisplayName(currency));
    newLine(builder);

    return builder;
  }

  /**
   * Returns the component which tells the user that they don't have enough money to buy the
   * protection
   *
   * @return The component
   */
  public static Component getTooExpensiveToBuyComponent() {
    return prefix().append(
        Component.text("Du hast nicht genügend Geld um dieses Grundstück zu kaufen.", ERROR));
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
   * Sends the user that he can buy the area
   *
   * @param user            the user
   * @param area            the area
   * @param effectiveCost   the effective cost
   * @param currency        the currency
   * @param pricePerBlock   The price per block
   * @param distanceToSpawn The distance to spawn
   */
  public static void sendAreaBuyableComponent(ProtectionUser user, long area, double effectiveCost,
      Currency currency, double pricePerBlock, double distanceToSpawn) {
    final Builder builder = buildAreaBuyHeader(area, effectiveCost, currency, pricePerBlock,
        distanceToSpawn);

    builder.append(Component.text("Wenn du das Grundstück kaufen möchtest,", INFO));
    newLine(builder);
    builder.append(Component.text("nutze den Bestätigungsknopf in deiner Hotbar.", INFO));

    user.sendMessage(builder.build());
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
   * Returns the component that tells the user that they are too far away from the protection start
   *
   * @return the component
   */
  public static Component getTooFarAwayFromStartComponent() {
    return prefix().append(
        Component.text("Du darfst dich nicht weiter von deinem Ausgangspunkt entfernen.", ERROR));
  }

  /**
   * Returns the component which tells the user that they are not standing in a user defined region
   *
   * @return the component
   */
  public static Component getNoPlayerDefinedRegionComponent() {
    return prefix().append(
        Component.text("Du stehst in keiner von einem Spieler gesicherten Region.", ERROR));
  }

  /**
   * Returns the pwho {@link Component}
   *
   * @param regionInfo the {@link RegionInfo}
   * @return the component
   */
  public static Component getPWhoComponent(RegionInfo regionInfo) {
    final ProtectionFlagInfo flagInfo = regionInfo.getProtectionFlagInfo();

    final String regionId = regionInfo.getRegion().getId();
    final String regionName = flagInfo != null ? flagInfo.getName() : null;
    final boolean existsAndDifferent = regionName != null && !regionName.equals(regionId);

    final Builder builder = Component.text();

    builder.append(PREFIX);
    builder.append(Component.text("Du befindest dich aktuell in der Region ", INFO));

    if (existsAndDifferent) {
      builder.append(Component.text(regionName + " (" + regionId + ")", VARIABLE_VALUE));
    } else {
      builder.append(Component.text(regionId, VARIABLE_VALUE));
    }

    builder.append(Component.text(". ", INFO));
    appendRegionOwnersMembersComponent(builder, regionInfo);

    return builder.asComponent();
  }

  /**
   * Returns the region owners and members component for the given region info
   *
   * @param builder    the builder
   * @param regionInfo The region info
   */
  public static void appendRegionOwnersMembersComponent(Builder builder, RegionInfo regionInfo) {
    final List<LocalPlayer> regionOwners = regionInfo.getOwners();
    final List<LocalPlayer> regionMembers = regionInfo.getMembers();

    if (regionOwners != null) {
      builder.append(Component.text("Besitzer: ", VARIABLE_KEY));
      appendRegionUsersComponent(builder, regionOwners);
    }

    if (regionOwners != null && regionMembers != null) {
      builder.append(Component.text(", ", SPACER));
    }

    if (regionMembers != null) {
      builder.append(Component.text("Mitglieder: ", VARIABLE_KEY));
      appendRegionUsersComponent(builder, regionMembers);
    }
  }

  /**
   * Returns the region user component for the given users
   *
   * @param builder     the builder
   * @param regionUsers The users
   * @return The region user component
   */
  public static void appendRegionUsersComponent(Builder builder, List<LocalPlayer> regionUsers) {
    final JoinConfiguration joinConfiguration = JoinConfiguration.builder()
        .prefix(Component.text("[", SPACER))
        .suffix(Component.text("]", SPACER))
        .separator(Component.text(", ", SPACER))
        .build();

    final List<Component> names = regionUsers.stream()
        .map(MessageManager::getDisplayName)
        .toList();

    builder.append(Component.join(joinConfiguration, names));
  }

  /**
   * Returns the component that informs the user about not having enough permissions
   *
   * @return the component
   */
  public static Component getNoPermissionComponent() {
    return prefix().append(
        Component.text("Du besitzt keine Berechtigung für diesen Befehl.", ERROR));
  }

  /**
   * Returns the component which is being shown to the user when he tries to rename a protection
   *
   * @param command  the command to run
   * @param currency the currency
   * @return the component
   */
  public static Component getProtectionRenameComponent(String command, Currency currency) {
    final Component clickComponent = Component.text("hier", VARIABLE_VALUE)
        .hoverEvent(HoverEvent.showText(getProtectionRenameHoverComponent(currency)))
        .clickEvent(ClickEvent.runCommand(command));

    return prefix()
        .append(Component.text("Um deine Region umzubenennen, klicke ", GRAY))
        .append(clickComponent)
        .append(Component.text(".", GRAY));
  }

  /**
   * Returns the component which is being shown to the user when he tries to rename a protection on
   * hover
   *
   * @param currency The currency
   * @return the component
   */
  public static Component getProtectionRenameHoverComponent(Currency currency) {
    Builder builder = Component.text();

    builder.append(Component.text("Klicke hier um deine Protection umzubenennen.", GRAY));

    builder.append(Component.newline());
    builder.append(Component.newline());
    builder.append(Component.text("ACHTUNG! ", RED, TextDecoration.BOLD));
    builder.append(Component.newline());
    builder.append(Component.text("Die Umbenennung kostet dich ", RED));
    builder.append(Component.text(ProtectionSettings.PROTECTION_RENAME_PRICE));
    builder.append(currencyDisplayName(currency));

    return builder.asComponent();
  }

  /**
   * Returns the currency display name
   *
   * @param currency the currency
   * @return The currency display name
   */
  private static Component currencyDisplayName(Currency currency) {
    return currency.getDisplayName().colorIfAbsent(VARIABLE_VALUE);
  }

  /**
   * Returns the component which tells the user that they are not standing on a protection
   *
   * @return the component
   */
  public static Component getNotStandingOnRenameProtectionComponent() {
    return prefix().append(
        Component.text("Du befindest dich nicht auf dem zu benennenden Grundstück.", ERROR));
  }

  /**
   * Returns the component which tells the user that they have renamed their protection
   *
   * @return the component
   */
  public static Component getProtectionRenamedComponent() {
    return prefix().append(
        Component.text("Du hast dein Grundstück erfolgreich umbenannt.", SUCCESS));
  }

  /**
   * Returns the component which tells the user that they have toggled visualization
   *
   * @param state the state
   * @return the component
   */
  public static Component getProtectionVisualizeComponent(boolean state) {
    return prefix()
        .append(Component.text("Du hast die Visualisierung der Grundstücke ", INFO))
        .append(Component.text(state ? "aktiviert" : "deaktiviert", state ? SUCCESS : ERROR))
        .append(Component.text(". Bitte warte einen kleinen Moment.", INFO));
  }

  /**
   * Returns the component which tells the user that a player could not be found
   *
   * @return the component
   */
  public static Component getPlayerNotFoundComponent() {
    return prefix().append(Component.text("Der Spieler wurde nicht gefunden.", ERROR));
  }

  /**
   * Returns the component which tells the user that they have sold a protection
   *
   * @param amount   The amount
   * @param currency The currency
   * @return the component
   */
  public static Component getProtectionSoldComponent(BigDecimal amount, Currency currency) {
    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    String formattedAmount = decimalFormat.format(amount);

    return prefix()
        .append(Component.text("Du hast dein Grundstück für ", INFO))
        .append(Component.text(formattedAmount, VARIABLE_VALUE))
        .append(Component.text(" ", VARIABLE_VALUE))
        .append(currencyDisplayName(currency))
        .append(Component.text(" verkauft.", INFO));
  }

  /**
   * Returns the compoinent which tells the user that they have used a protection without a teleport
   * point
   *
   * @return The component
   */
  public static Component getNoTeleportLocationComponent() {
    return prefix().append(Component.text("Es wurde kein Teleportationspunkt gefunden.", ERROR));
  }

  /**
   * Gets protection already processing component.
   *
   * @return the protection already processing component
   */
  public static Component getProtectionAlreadyProcessingComponent() {
    return prefix().append(Component.text("Bitte gedulde dich einen Moment.", ERROR));
  }

  /**
   * Gets display name.
   *
   * @param localPlayer the local player
   * @return the display name
   */
  private static @NotNull Component getDisplayName(@NotNull LocalPlayer localPlayer) {
    String name = localPlayer.getName();

    if (name == null) {
      Profile profile = WorldGuard.getInstance().getProfileCache()
          .getIfPresent(localPlayer.getUniqueId());
      if (profile != null) {
        name = profile.getName();
      }
    }
    return Component.text(name == null ? "#UNKNOWN" : name, VARIABLE_VALUE);
  }

  /**
   * Gets plot messages changed component.
   *
   * @param newState the new state
   * @return the plot messages changed component
   */
  public static Component getPlotMessagesChangedComponent(boolean newState) {
    return prefix()
        .append(Component.text("Die Nachrichten beim Betreten/Verlassen eines Grundstücks wurden ",
            INFO))
        .append(Component.text(newState ? "aktiviert" : "deaktiviert", newState ? SUCCESS : ERROR))
        .append(Component.text(".", INFO));
  }
}
