package dev.slne.protect.bukkit.gui.list;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import dev.slne.protect.bukkit.gui.utils.ConfirmationGui;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.transaction.core.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionListOneGui extends ChestGui {

    private ProtectedRegion region;
    private long area;
    private double distance;
    private List<String> ownerNames;
    private List<String> memberNames;
    private RegionInfo regionInfo;

    /**
     * Creates a new gui for a region
     *
     * @param region      The region
     * @param area        The area
     * @param distance    The distance
     * @param ownerNames  The owners
     * @param memberNames The members
     * @param regionInfo  The region info
     */
    @SuppressWarnings("java:S2589")
    public ProtectionListOneGui(ProtectedRegion region, long area, long distance,
            List<String> ownerNames,
            List<String> memberNames, RegionInfo regionInfo, Player viewingPlayer) {
        super(5, regionInfo.getName());

        this.regionInfo = regionInfo;
        this.region = region;
        this.area = area;
        this.distance = distance;
        this.ownerNames = ownerNames;
        this.memberNames = memberNames;

        Location teleportLocation = regionInfo.getTeleportLocation();

        setOnGlobalClick(event -> event.setCancelled(true));

        GuiItem regionNameItem = new GuiItem(
                ItemStackUtils.getItem(Material.NAME_TAG, 1, 0,
                        Component.text(regionInfo.getName(), NamedTextColor.RED)));

        OutlinePane backgroundPane = new OutlinePane(0, 0, 9, 1);
        backgroundPane.addItem(new GuiItem(
                ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space())));
        backgroundPane.setPriority(Pane.Priority.LOWEST);
        backgroundPane.setRepeat(true);

        OutlinePane backgroundPane2 = new OutlinePane(0, 4, 9, 1);
        backgroundPane2.addItem(new GuiItem(
                ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space())));
        backgroundPane2.setPriority(Pane.Priority.LOWEST);
        backgroundPane2.setRepeat(true);

        StaticPane staticPane = new StaticPane(0, 0, 9, 5);

        // Row 1
        staticPane.addItem(regionNameItem, 4, 0);

        // Row 2
        if (viewingPlayer.hasPermission("surf.protect.view.area")) {
            staticPane.addItem(getAreaItem(area), 1, 1);
        }

        if (viewingPlayer.hasPermission("surf.protect.view.distance")) {
            staticPane.addItem(getDistanceItem(distance), 2, 1);
        }

        if (teleportLocation != null && viewingPlayer.hasPermission("surf.protect.view.location")) {
            staticPane.addItem(getLocationItem(teleportLocation), 3, 1);
        }

        if (viewingPlayer.hasPermission("surf.protect.view.owners")) {
            staticPane.addItem(getOwnersItem(ownerNames), 6, 1);
        }

        if (viewingPlayer.hasPermission("surf.protect.view.members")) {
            staticPane.addItem(getMembersItem(memberNames), 7, 1);
        }

        // Row 3
        if (teleportLocation != null && viewingPlayer.hasPermission("surf.protect.view.teleport")) {
            staticPane.addItem(getTeleportItem(teleportLocation), 4, 2);
        }

        // Row 4
        if (viewingPlayer.hasPermission("surf.protect.view.expand")) {
            staticPane.addItem(getProtectionExpandItem(), 1, 3);
        }

        if (viewingPlayer.hasPermission("surf.protect.view.sell")) {
            staticPane.addItem(getProtectionSellItem(), 7, 3);
        }

        // Row 5
        staticPane.addItem(
                new GuiItem(ItemStackUtils.getCloseItemStack(), event -> event.getWhoClicked().closeInventory()), 4, 4);

        addPane(backgroundPane);
        addPane(backgroundPane2);
        addPane(staticPane);
    }

    /**
     * Returns the item for the protection expand
     *
     * @return The item
     */
    private GuiItem getProtectionExpandItem() {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Erweitert das Grundstück", NamedTextColor.GRAY));
        lore.add(Component.empty());

        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.empty());
        confirmLore
                .addAll(ItemStackUtils.splitComponent("Bist du dir sicher, dass du das Grundstück erweitern möchtest?",
                        50, MessageManager.VARIABLE_VALUE));

        return new GuiItem(ItemStackUtils.getItem(Material.GRASS_BLOCK, 1, 0,
                Component.text("Grundstück erweitern", MessageManager.VARIABLE_KEY), lore), event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();

                    ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
                        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
                        ProtectionRegion protectionRegion = new ProtectionRegion(protectionUser,
                                regionInfo.getRegion());

                        ProtectedRegion protectedRegion = regionInfo.getRegion();
                        State canSellState = protectedRegion.getFlag(ProtectionFlags.SURF_CAN_SELL_FLAG);
                        boolean canExpand = canSellState == State.ALLOW || canSellState == null;

                        if (!canExpand) {
                            protectionUser.sendMessage(MessageManager.prefix()
                                    .append(Component.text("Das Grundstück darf nicht erweitert werden.",
                                            MessageManager.ERROR)));
                            return;
                        }

                        if (ProtectionUtils.standsInProtectedRegion(protectionUser.getBukkitPlayer(),
                                regionInfo.getRegion())) {
                            protectionUser.startRegionCreation(protectionRegion);
                            protectionRegion.setExpandingMarkers();

                            MessageManager.sendProtectionModeEnterMessages(protectionUser);
                        } else {
                            protectionUser.sendMessage(MessageManager.prefix().append(Component.text(
                                    "Du befindest dich nicht auf dem zu erweiternden Grundstück.",
                                    MessageManager.ERROR)));
                        }
                        confirmEvent.getWhoClicked().closeInventory();
                    }, cancelEvent -> {

                    }, Component.text("Grundstück erweitern", MessageManager.VARIABLE_KEY), confirmLore);

                    confirmationGui.show(player);
                });
    }

    /**
     * Returns the item for the protection sell
     */
    @SuppressWarnings("java:S3776")
    private GuiItem getProtectionSellItem() {
        return new GuiItem(ItemStackUtils.getItem(Material.BEDROCK, 1, 0,
                Component.text("Grundstück löschen", NamedTextColor.RED), Component.empty(),
                Component.text("Löscht das Grundstück", NamedTextColor.GRAY), Component.empty()), event -> {
                    Player player = (Player) event.getWhoClicked();

                    List<Component> confirmLore = new ArrayList<>();
                    confirmLore.add(Component.empty());
                    confirmLore.addAll(ItemStackUtils.splitComponent(
                            "Bist du dir sicher, dass du das Grundstück löschen möchtest?", 50, MessageManager.ERROR));
                    confirmLore.add(Component.empty());
                    confirmLore.add(Component.text("Achtung: Das Grundstück kann nicht wiederhergestellt werden!",
                            MessageManager.ERROR));
                    confirmLore.add(Component.empty());
                    confirmLore
                            .add(Component.text("Das Grundstück wird dir für einen Anteil des Kaufpreises erstattet.",
                                    NamedTextColor.GRAY));

                    ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
                        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

                        ProtectedRegion protectedRegion = regionInfo.getRegion();
                        State canSellState = protectedRegion.getFlag(ProtectionFlags.SURF_CAN_SELL_FLAG);
                        boolean canSell = canSellState == State.ALLOW || canSellState == null;

                        if (!canSell) {
                            protectionUser.sendMessage(MessageManager.prefix()
                                    .append(Component.text("Das Grundstück darf nicht verkauft werden.",
                                            MessageManager.ERROR)));
                            return;
                        }

                        if (isRegionEdited()) {
                            protectionUser.sendMessage(MessageManager.prefix()
                                    .append(Component.text("Das Grundstück wird gerade bearbeitet!",
                                            MessageManager.ERROR)));
                            return;
                        }

                        List<String> members = new ArrayList<>();
                        members.addAll(protectedRegion.getOwners().getPlayers());
                        members.addAll(protectedRegion.getMembers().getPlayers());

                        for (String member : members) {
                            Player memberPlayer = Bukkit.getPlayer(member);

                            if (!memberPlayer.isOnline()) {
                                continue;
                            }

                            notifyDeletion(player, regionInfo);
                        }

                        BigDecimal refund = BigDecimal.valueOf(regionInfo.getRetailPrice());

                        Optional<Currency> currencyOptional = Currency.currencyByName("CastCoin");

                        if (currencyOptional.isEmpty()) {
                            protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
                            return;
                        }

                        Currency currency = currencyOptional.get();

                        RegionManager regionManager = ProtectionUtils.getRegionManager(player.getWorld());
                        regionManager.removeRegion(protectedRegion.getId());

                        protectionUser.addTransaction(null, refund, currency);
                        confirmEvent.getWhoClicked().closeInventory();
                    }, cancelEvent -> {

                    }, Component.text("Grundstück löschen?", NamedTextColor.RED), confirmLore);

                    confirmationGui.show(player);
                });
    }

    /**
     * Returns whether the region is edited
     *
     * @return Whether the region is edited
     */
    private boolean isRegionEdited() {
        List<ProtectionUser> protectionUsers = BukkitMain.getBukkitInstance().getUserManager().getUsers();
        boolean isEdited = false;

        for (ProtectionUser user : protectionUsers) {
            if (user.hasRegionCreation()) {
                ProtectionRegion protectionRegion = user.getRegionCreation();
                ProtectedRegion protectedRegion = protectionRegion.getExpandingProtection();

                if (protectedRegion != null && protectedRegion.getId().equals(region.getId())) {
                    isEdited = true;
                    break;
                }
            }
        }

        return isEdited;
    }

    /**
     * Notifies the player about the deletion
     *
     * @param player     The player
     * @param regionInfo The region info
     */
    private void notifyDeletion(Player player, RegionInfo regionInfo) {
        player.sendMessage(MessageManager.prefix().append(Component.text("Das Grundstück ", MessageManager.INFO))
                .append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_VALUE))
                .append(Component.text(" wurde verkauft.", MessageManager.INFO)));
    }

    /**
     * Returns the item for the area
     *
     * @param area The area
     * @return The item
     */
    private GuiItem getAreaItem(long area) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text(area + " Blöcke", MessageManager.VARIABLE_VALUE));
        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.WOODEN_AXE, 1, 0,
                Component.text("Fläche", MessageManager.VARIABLE_KEY), lore));
    }

    /**
     * Returns the item for the location
     *
     * @param teleportLocation The teleport location
     * @return The item
     */
    private GuiItem getLocationItem(Location teleportLocation) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Welt: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(teleportLocation.getWorld().getName(), MessageManager.VARIABLE_VALUE)));
        lore.add(Component.text("X: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(teleportLocation.getBlockX(), MessageManager.VARIABLE_VALUE)));
        lore.add(Component.text("Y: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(teleportLocation.getBlockY(), MessageManager.VARIABLE_VALUE)));
        lore.add(Component.text("Z: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(teleportLocation.getBlockZ(), MessageManager.VARIABLE_VALUE)));
        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.COMPASS, 1, 0,
                Component.text("Position", MessageManager.VARIABLE_KEY), lore));
    }

    /**
     * Returns the item for the distance
     *
     * @param distance The distance
     * @return The item
     */
    private GuiItem getDistanceItem(double distance) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text(distance + " Blöcke", MessageManager.VARIABLE_VALUE));
        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.COMPASS, 1, 0,
                Component.text("Entfernung", MessageManager.VARIABLE_KEY), lore));
    }

    /**
     * Returns the item for the owners
     *
     * @param owners The owners
     * @return The item
     */
    private GuiItem getOwnersItem(List<String> owners) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        Component none = Component.text("Keine", MessageManager.VARIABLE_VALUE);

        if (owners.isEmpty()) {
            lore.add(none);
        } else {
            lore.addAll(ItemStackUtils.splitComponent(String.join(", ", owners), 50, MessageManager.VARIABLE_VALUE));
        }

        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.PLAYER_HEAD, 1, 0,
                Component.text("Besitzer", MessageManager.VARIABLE_KEY), lore));
    }

    /**
     * Returns the item for the members
     *
     * @param members The members
     * @return The item
     */
    private GuiItem getMembersItem(List<String> members) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        Component none = Component.text("Keine", MessageManager.VARIABLE_VALUE);

        if (members.isEmpty()) {
            lore.add(none);
        } else {
            lore.addAll(ItemStackUtils.splitComponent(String.join(", ", members), 50, MessageManager.VARIABLE_VALUE));
        }

        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.PLAYER_HEAD, 1, 0,
                Component.text("Mitglieder", MessageManager.VARIABLE_KEY), lore));
    }

    /**
     * Returns the item for the teleport
     *
     * @return The item
     */
    private GuiItem getTeleportItem(Location location) {
        List<Component> lore = new ArrayList<>();

        int highestY = location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
        location.setY(highestY + (double) 1);

        lore.add(Component.empty());
        lore.add(Component.text("Teleportiert dich zu der Region", NamedTextColor.GRAY));
        lore.add(Component.empty());

        return new GuiItem(ItemStackUtils.getItem(Material.ENDER_PEARL, 1, 0,
                Component.text("Teleportieren", MessageManager.VARIABLE_KEY), lore), event -> {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().teleportAsync(location);
                });
    }

    /**
     * @return the area
     */
    public long getArea() {
        return area;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return the memberNames
     */
    public List<String> getMemberNames() {
        return memberNames;
    }

    /**
     * @return the ownerNames
     */
    public List<String> getOwnerNames() {
        return ownerNames;
    }

    /**
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    /**
     * @return the regionInfo
     */
    public RegionInfo getRegionInfo() {
        return regionInfo;
    }

}
