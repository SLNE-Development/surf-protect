package dev.slne.protect.bukkit.message;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.transaction.core.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Represents the message manager
 */
public class MessageManager {

    public static final TextColor PRIMARY = TextColor.fromHexString("#3b92d1");
    public static final TextColor SECONDARY = TextColor.fromHexString("#5b5b5b");

    public static final TextColor INFO = TextColor.fromHexString("#40d1db");
    public static final TextColor SUCCESS = TextColor.fromHexString("#65ff64");
    public static final TextColor WARNING = TextColor.fromHexString("#f9c353");
    public static final TextColor ERROR = TextColor.fromHexString("#ee3d51");

    public static final TextColor VARIABLE_KEY = MessageManager.INFO;
    public static final TextColor VARIABLE_VALUE = MessageManager.WARNING;
    public static final TextColor SPACER = NamedTextColor.GRAY;
    public static final TextColor DARK_SPACER = NamedTextColor.DARK_GRAY;

    /**
     * Utility class
     */
    private MessageManager() {
        throw new IllegalStateException("Utility class");
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
     * Returns a prefix for the plugin.
     *
     * @return The prefix for the plugin.
     */
    public static Component prefix() {
        TextComponent.Builder builder = Component.text();

        builder.append(Component.text(">> ", NamedTextColor.DARK_GRAY));
        builder.append(Component.text("SP", MessageManager.PRIMARY));
        builder.append(Component.text(" | ", NamedTextColor.DARK_GRAY));

        return builder.build();
    }

    /**
     * Returns a component which tells the user that they need to place more markers
     *
     * @param placedMarkers The amount of placed markers
     *
     * @return The component
     */
    public static Component getMoreMarkersComponent(int placedMarkers) {
        int neededMarkers = ProtectionSettings.MIN_MARKERS - placedMarkers;

        return prefix().append(Component.text("Du musst mindestens ", ERROR))
                .append(Component.text(String.valueOf(neededMarkers), VARIABLE_VALUE))
                .append(Component.text(" weitere Marker platzieren.", ERROR));
    }

    /**
     * Returns a component which tells the user that the region overlaps with
     * another region
     *
     * @return The component
     */
    public static Component getOverlappingRegionsComponent() {
        return prefix().append(Component.text("Die markierte Fläche kollidiert mit einem anderen Grundstück.", ERROR));
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
     * Sends an empty line to the user
     *
     * @param user The user
     */
    private static void emptyLine(ProtectionUser user) {
        prefixMessage(user, Component.empty());
    }

    /**
     * Sends the area buy header to the user
     *
     * @param user            The user
     * @param area            The area
     * @param effectiveCost   The effective cost
     * @param currency        The currency
     * @param pricePerBlock   The price per block
     * @param distanceToSpawn The distance to spawn
     */
    private static void sendAreaBuyHeader(ProtectionUser user, long area, double effectiveCost, Currency currency,
                                          double pricePerBlock, double distanceToSpawn) {
        distanceToSpawn = Math.round(distanceToSpawn * 100.0) / 100.0;

        emptyLine(user);
        prefixMessage(user, Component.text("Das Grundstück steht zum Verkauf", SUCCESS));
        emptyLine(user);
        prefixMessage(user, Component.text("Fläche: ", VARIABLE_KEY).append(Component.text(area, VARIABLE_VALUE))
                .append(Component.text(" Blöcke", VARIABLE_VALUE)));
        prefixMessage(user,
                Component.text("Preis/Block: ", VARIABLE_KEY).append(Component.text(pricePerBlock, VARIABLE_VALUE))
                        .appendSpace().append(currencyDisplayName(currency)));
        prefixMessage(user,
                Component.text("Distanz zum Spawn: ", VARIABLE_KEY).append(Component.text(distanceToSpawn,
                        VARIABLE_VALUE)).append(Component.text(" Blöcke", VARIABLE_VALUE)));
        prefixMessage(user,
                Component.text("Kaufpreis: ", VARIABLE_KEY).append(Component.text(effectiveCost, VARIABLE_VALUE))
                        .appendSpace().append(currencyDisplayName(currency)));
        emptyLine(user);
    }

    /**
     * Returns the component which tells the user that they don't have enough money
     * to buy the protection
     *
     * @return The component
     */
    public static Component getTooExpensiveToBuyComponent() {
        return prefix().append(Component.text("Du hast nicht genügend Geld um dieses Grundstück zu kaufen.", ERROR));
    }

    /**
     * Returns the component which tells the user that they don't have enough money
     * to rename the protection
     *
     * @return The component
     */
    public static Component getTooExpensiveToRenameComponent() {
        return prefix().append(Component.text("Du hast nicht genügend Geld um dieses Grundstück umzubenennen.", ERROR));
    }

    /**
     * Sends the user that the region is too expensive
     *
     * @param user            The user
     * @param area            The area
     * @param effectiveCost   The effective cost
     * @param currency        The currency
     * @param pricePerBlock   The price per block
     * @param distanceToSpawn The distance to spawn
     */
    public static void sendAreaTooExpensiveComponent(ProtectionUser user, long area, double effectiveCost,
                                                     Currency currency, double pricePerBlock, double distanceToSpawn) {
        sendAreaBuyHeader(user, area, effectiveCost, currency, pricePerBlock, distanceToSpawn);

        prefixMessage(user, getTooExpensiveToBuyComponent());
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
        sendAreaBuyHeader(user, area, effectiveCost, currency, pricePerBlock, distanceToSpawn);

        prefixMessage(user, Component.text("Wenn du das Grundstück kaufen möchtest,", INFO));
        prefixMessage(user, Component.text("nutze den Bestätigungsknopf in deiner Hotbar.", INFO));
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
        return prefix().append(Component.text("Du hast die Erstellung deines Grundstücks abgebrochen.", ERROR));
    }

    /**
     * Returns the component that tells the user that they are too far away from the
     * protection start
     *
     * @return the component
     */
    public static Component getTooFarAwayFromStartComponent() {
        return prefix().append(
                Component.text("Du darfst dich nicht weiter von deinem Ausgangspunkt entfernen.", ERROR));
    }

    /**
     * Returns the component which tells the user that they are not standing in a
     * user defined region
     *
     * @return the component
     */
    public static Component getNoPlayerDefinedRegionComponent() {
        return prefix().append(Component.text("Du stehst in keiner von einem Spieler gesicherten Region.", ERROR));
    }

    /**
     * Returns the pwho {@link Component}
     *
     * @param regionInfo the {@link RegionInfo}
     *
     * @return the component
     */
    public static Component getPWhoComponent(RegionInfo regionInfo) {
        String regionId = regionInfo.getRegion().getId();
        String regionName =
                regionInfo.getProtectionFlagInfo() != null ? regionInfo.getProtectionFlagInfo().getName() : null;

        boolean existsAndDifferent = regionName != null && !regionName.equals(regionId);

        TextComponent.Builder builder = Component.text();
        builder.append(prefix());
        builder.append(Component.text("Du befindest dich aktuell in der Region ", INFO));

        if (existsAndDifferent) {
            builder.append(Component.text(regionName + " (" + regionId + ")", VARIABLE_VALUE));
        } else {
            builder.append(Component.text(regionId, VARIABLE_VALUE));
        }

        builder.append(Component.text(". ", INFO));
        builder.append(getRegionOwnersMembersComponent(regionInfo));

        return builder.asComponent();
    }

    /**
     * Returns the region owners and members component for the given region info
     *
     * @param regionInfo The region info
     *
     * @return The region owners and members component
     */
    public static Component getRegionOwnersMembersComponent(RegionInfo regionInfo) {
        TextComponent.Builder builder = Component.text();

        List<LocalPlayer> regionOwners = regionInfo.getOwners();
        List<LocalPlayer> regionMembers = regionInfo.getMembers();

        if (regionOwners != null) {
            builder.append(Component.text("Besitzer: ", VARIABLE_KEY));
            builder.append(getRegionUsersComponent(regionOwners));
        }

        if (regionOwners != null && regionMembers != null) {
            builder.append(Component.text(", ", SPACER));
        }

        if (regionMembers != null) {
            builder.append(Component.text("Mitglieder: ", VARIABLE_KEY));
            builder.append(getRegionUsersComponent(regionMembers));
        }

        return builder.build();
    }

    /**
     * Returns the region user component for the given users
     *
     * @param regionUsers The users
     *
     * @return The region user component
     */
    public static Component getRegionUsersComponent(List<LocalPlayer> regionUsers) {
        TextComponent.Builder memberComponentBuilder = Component.text();
        Iterator<LocalPlayer> memberIterator = regionUsers.iterator();

        memberComponentBuilder.append(Component.text("[", SPACER));

        ProfileCache cache = WorldGuard.getInstance().getProfileCache();
        while (memberIterator.hasNext()) {
            LocalPlayer memberUser = memberIterator.next();
            String userName = memberUser.getName();

            if (userName == null) {
                Profile profile = cache.getIfPresent(memberUser.getUniqueId());

                if (profile != null) {
                    userName = profile.getName();
                }
            }

            if (userName != null) {
                memberComponentBuilder.append(Component.text(userName, VARIABLE_VALUE));

                if (memberIterator.hasNext()) {
                    memberComponentBuilder.append(Component.text(", ", SPACER));
                }
            }
        }

        memberComponentBuilder.append(Component.text("]", SPACER));

        return memberComponentBuilder.build();
    }

    /**
     * Returns the component that informs the user about not having enough
     * permissions
     *
     * @return the component
     */
    public static Component getNoPermissionComponent() {
        return prefix().append(Component.text("Du besitzt keine Berechtigung für diesen Befehl.", ERROR));
    }

    /**
     * Returns the component which is being shown to the user when he tries to
     * rename a protection
     *
     * @param command the command to run
     *
     * @return the component
     */
    public static Component getProtectionRenameComponent(String command, Currency currency) {
        ClickEvent clickEvent = ClickEvent.runCommand(command);
        HoverEvent<Component> hoverEvent =
                HoverEvent.showText(MessageManager.getProtectionRenameHoverComponent(currency));

        Component clickComponent =
                Component.text("hier", MessageManager.VARIABLE_VALUE).clickEvent(clickEvent).hoverEvent(hoverEvent);

        return prefix().append(Component.text("Um deine Region umzubenennen, klicke ", NamedTextColor.GRAY))
                .append(clickComponent).append(Component.text(".", NamedTextColor.GRAY));
    }

    /**
     * Returns the component which is being shown to the user when he tries to
     * rename a protection on hover
     *
     * @param currency The currency
     *
     * @return the component
     */
    public static Component getProtectionRenameHoverComponent(Currency currency) {
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("Klicke hier um deine Protection umzubenennen.", NamedTextColor.GRAY));

        builder.append(Component.newline());
        builder.append(Component.newline());
        builder.append(Component.text("ACHTUNG! ", NamedTextColor.RED, TextDecoration.BOLD));
        builder.append(Component.newline());
        builder.append(Component.text("Die Umbenennung kostet dich ", NamedTextColor.RED));
        builder.append(Component.text(ProtectionSettings.PROTECTION_RENAME_PRICE));
        builder.append(currencyDisplayName(currency));

        return builder.asComponent();
    }

    /**
     * Returns the currency display name
     *
     * @return The currency display name
     */
    private static Component currencyDisplayName(Currency currency) {
        return currency.displayName().colorIfAbsent(VARIABLE_VALUE);
    }

    /**
     * Returns the component which tells the user that they are not standing on a
     * protection
     *
     * @return the component
     */
    public static Component getNotStandingOnRenameProtectionComponent() {
        return prefix().append(Component.text("Du befindest dich nicht auf dem zu benennenden Grundstück.", ERROR));
    }

    /**
     * Returns the component which tells the user that they have renamed their
     * protection
     *
     * @return the component
     */
    public static Component getProtectionRenamedComponent() {
        return prefix().append(Component.text("Du hast dein Grundstück erfolgreich umbenannt.", SUCCESS));
    }

    /**
     * Returns the component which tells the user that they have toggled
     * visualization
     *
     * @return the component
     */
    public static Component getProtectionVisualizeComponent(boolean state) {
        return prefix().append(Component.text("Du hast die Visualisierung der Grundstücke ", INFO))
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
     * Sends a prefixed message to the user
     *
     * @param user    The user
     * @param message The message
     */
    private static void prefixMessage(ProtectionUser user, Component message) {
        user.sendMessage(prefix().append(message));
    }

    /**
     * Returns the component which tells the user that they have sold a protection
     *
     * @param amount   The amount
     * @param currency The currency
     *
     * @return the component
     */
    public static Component getProtectionSoldComponent(BigDecimal amount, Currency currency) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedAmount = decimalFormat.format(amount);

        return prefix().append(Component.text("Du hast dein Grundstück für ", INFO))
                .append(Component.text(formattedAmount, MessageManager.VARIABLE_VALUE))
                .append(Component.text(" ", MessageManager.VARIABLE_VALUE)).append(currencyDisplayName(currency))
                .append(Component.text(" verkauft.", INFO));
    }

    /**
     * Returns the compoinent which tells the user that they have used a protection without a teleport point
     *
     * @return The component
     */
    public static Component getNoTeleportLocationComponent() {
        return prefix().append(Component.text("Es wurde kein Teleportationspunkt gefunden.", ERROR));
    }
}
