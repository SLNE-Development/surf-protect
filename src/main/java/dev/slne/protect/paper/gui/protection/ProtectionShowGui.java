package dev.slne.protect.paper.gui.protection;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.chest.SurfChestGui;
import dev.slne.protect.paper.gui.confirmation.ConfirmationGui;
import dev.slne.protect.paper.gui.protection.flags.ProtectionFlagsGui;
import dev.slne.protect.paper.gui.protection.members.ProtectionMembersGui;
import dev.slne.protect.paper.gui.protection.rename.RenameAnvilGUI;
import dev.slne.protect.paper.gui.utils.ItemUtils;
import dev.slne.protect.paper.message.MessageManager;
import dev.slne.surf.protect.paper.PaperMain;
import dev.slne.surf.protect.paper.region.ProtectionRegion;
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry;
import dev.slne.surf.protect.paper.region.info.RegionInfo;
import dev.slne.surf.protect.paper.region.settings.ProtectionSettings;
import dev.slne.surf.protect.paper.region.transaction.ProtectionSellData;
import dev.slne.surf.protect.paper.user.ProtectionUser;
import dev.slne.surf.protect.paper.user.ProtectionUserManager;
import dev.slne.surf.protect.paper.util.UtilKt;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ProtectionShowGui extends SurfChestGui {

  private final ProtectedRegion region;
  private final long area;
  private final double distance;
  private final RegionInfo regionInfo;
  private final StaticPane staticPane;

  private final Player viewingPlayer;

  /**
   * Creates a new gui for a region
   *
   * @param region        The region
   * @param area          The area
   * @param distance      The distance
   * @param regionInfo    The region info
   * @param viewingPlayer The viewing player
   */
  @SuppressWarnings("java:S2589")
  public ProtectionShowGui(SurfGui parentGui, ProtectedRegion region, long area, double distance,
      RegionInfo regionInfo, Player viewingPlayer) {
    super(parentGui, 5, Component.text(regionInfo.getName()));

    this.viewingPlayer = viewingPlayer;
    this.regionInfo = regionInfo;
    this.region = region;
    this.area = area;
    this.distance = distance;

    Location teleportLocation = Optional.ofNullable(regionInfo.getCenterLocation()).map(
        BukkitAdapter::adapt).orElse(null);

    GuiItem regionNameItem = new GuiItem(ItemUtils.item(Material.NAME_TAG, 1, 0,
        Component.text(regionInfo.getName(), MessageManager.PRIMARY)));

    staticPane = new StaticPane(0, 0, 9, 5);

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

    // Row 3
    if (teleportLocation != null && viewingPlayer.hasPermission("surf.protect.view.teleport")) {
      staticPane.addItem(getTeleportItem(teleportLocation), 4, 2);
    }

    // Row 4
    if (viewingPlayer.hasPermission("surf.protect.view.expand")) {
      staticPane.addItem(getProtectionExpandItem(), 1, 3);
    }

    if (viewingPlayer.hasPermission("surf.protect.view.rename")) {
      staticPane.addItem(getRenameItem(), 2, 3);
    }

    if (viewingPlayer.hasPermission("surf.protect.flags.edit")) {
      staticPane.addItem(getFlagsItem(), 3, 3);
    }

    if (viewingPlayer.hasPermission("surf.protect.view.sell")) {
      staticPane.addItem(getProtectionSellItem(), 7, 3);
    }

    addPane(staticPane);
    update();
  }

  @Override
  public void update() {
    if (viewingPlayer.hasPermission("surf.protect.view.owners")) {
      staticPane.addItem(getOwnersItem(), 6, 1);
    }

    if (viewingPlayer.hasPermission("surf.protect.view.members")) {
      staticPane.addItem(getMembersItem(), 7, 1);
    }

    ProtectionShowGui.super.update();
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

    return new GuiItem(
        ItemUtils.item(Material.WOODEN_AXE, 1, 0, Component.text("Fläche", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)));
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

    return new GuiItem(
        ItemUtils.item(Material.COMPASS, 1, 0, Component.text("Entfernung", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)));
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
    lore.add(Component.text("Welt: ", NamedTextColor.GRAY)
        .append(
            Component.text(teleportLocation.getWorld().getName(), MessageManager.VARIABLE_VALUE)));
    lore.add(Component.text("X: ", NamedTextColor.GRAY)
        .append(Component.text(teleportLocation.getBlockX(), MessageManager.VARIABLE_VALUE)));
    lore.add(Component.text("Y: ", NamedTextColor.GRAY)
        .append(Component.text(teleportLocation.getBlockY(), MessageManager.VARIABLE_VALUE)));
    lore.add(Component.text("Z: ", NamedTextColor.GRAY)
        .append(Component.text(teleportLocation.getBlockZ(), MessageManager.VARIABLE_VALUE)));
    lore.add(Component.empty());

    return new GuiItem(
        ItemUtils.item(Material.COMPASS, 1, 0, Component.text("Position", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)));
  }

  /**
   * Returns the rename item
   *
   * @return The item
   */
  private GuiItem getRenameItem() {
    return new GuiItem(ItemUtils.item(Material.NAME_TAG, 1, 0,
        Component.text("Grundstück umbenennen", MessageManager.PRIMARY),
        Component.empty(),
        Component.text("Ermöglicht es dir dein Grundstück umzubenennen", NamedTextColor.GRAY),
        Component.empty(), Component.text("Achtung:", NamedTextColor.RED)
            .append(Component.text(" Für diese Aktion wird eine Gebühr in Höhe von ",
                    NamedTextColor.GRAY)
                .append(Component.text(ProtectionSettings.PROTECTION_RENAME_PRICE + " CastCoins",
                    NamedTextColor.YELLOW))
                .append(Component.text(" berechnet.", NamedTextColor.GRAY)))), event -> {
      RenameAnvilGUI renameAnvilGui = new RenameAnvilGUI(this, region);
      renameAnvilGui.show(event.getWhoClicked());
    });
  }

  /**
   * Returns the item for the owners
   *
   * @return The item
   */
  private GuiItem getOwnersItem() {
    List<String> ownerNames = UtilKt.getOwnerNames(getRegion());
    List<Component> lore = new ArrayList<>();

    lore.add(Component.empty());
    if (ownerNames.isEmpty()) {
      lore.add(Component.text("Keine", MessageManager.VARIABLE_VALUE));
    } else {
      lore.addAll(ItemUtils.splitComponent(String.join(", ", ownerNames), 50,
          MessageManager.VARIABLE_VALUE));
    }
    lore.add(Component.empty());

    return new GuiItem(
        ItemUtils.item(Material.PLAYER_HEAD, 1, 0,
            Component.text("Besitzer", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)));
  }

  /**
   * Returns the item for the members
   *
   * @return The item
   */
  private GuiItem getMembersItem() {
    List<String> memberNames = UtilKt.getMemberNames(getRegion());
    List<Component> lore = new ArrayList<>();

    lore.add(Component.empty());
    if (memberNames.isEmpty()) {
      lore.add(Component.text("Keine", MessageManager.VARIABLE_VALUE));
    } else {
      lore.addAll(ItemUtils.splitComponent(String.join(", ", memberNames), 50,
          MessageManager.VARIABLE_VALUE));
    }
    lore.add(Component.empty());

    return new GuiItem(ItemUtils.item(Material.PLAYER_HEAD, 1, 0, Component.text("Mitglieder",
        MessageManager.PRIMARY), lore.toArray(Component[]::new)), event -> {
      ProtectionMembersGui membersGui = new ProtectionMembersGui(this, region);
      membersGui.show(event.getWhoClicked());
    });
  }

  /**
   * Returns the item for the teleport
   *
   * @return The item
   */
  private GuiItem getTeleportItem(Location location) {
    List<Component> lore = new ArrayList<>();

    int highestY = location.getWorld()
        .getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
    location.setY(highestY + (double) 1);

    lore.add(Component.empty());
    lore.add(Component.text("Teleportiert dich zu der Region", NamedTextColor.GRAY));
    lore.add(Component.empty());

    return new GuiItem(
        ItemUtils.item(Material.ENDER_PEARL, 1, 0,
            Component.text("Teleportieren", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)),
        event -> {
          event.getWhoClicked().closeInventory();
          event.getWhoClicked().teleportAsync(location);
        });
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

    List<Component> confirmLore = new ArrayList<>(
        ItemUtils.splitComponent("Bist du dir sicher, dass du das Grundstück erweitern möchtest?",
            50,
            NamedTextColor.GRAY));

    return new GuiItem(
        ItemUtils.item(Material.GRASS_BLOCK, 1, 0,
            Component.text("Grundstück erweitern", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)),
        event -> {
          Player player = (Player) event.getWhoClicked();
          player.closeInventory();

//          ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
//            ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
//            ProtectionRegion protectionRegion = new ProtectionRegion(protectionUser,
//                regionInfo.region);
//
//            ProtectedRegion protectedRegion = regionInfo.region;
//            State canSellState = protectedRegion.getFlag(
//                ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG);
//            boolean canExpand = canSellState == State.ALLOW || canSellState == null;
//
//            if (!canExpand) {
//              protectionUser.sendMessage(MessageManager.prefix()
//                  .append(Component.text("Das Grundstück darf nicht erweitert werden.",
//                      MessageManager.ERROR)));
//              return;
//            }
//
//            confirmEvent.getWhoClicked().closeInventory();
//
//            if (UtilKt.standsInProtectedRegion(protectionUser.getBukkitPlayer(),
//                regionInfo.region)) {
//              if (protectionUser.startRegionCreation(protectionRegion)) {
//                protectionRegion.setCornerMarkers();
//              }
//            } else {
//              protectionUser.sendMessage(MessageManager.prefix()
//                  .append(Component.text(
//                      "Du befindest dich nicht auf dem zu erweiternden Grundstück.",
//                      MessageManager.ERROR)));
//            }
//          }, (cancelEvent, parent) -> {
//            if (!(cancelEvent instanceof InventoryCloseEvent closeEvent && closeEvent.getReason()
//                .equals(
//                    InventoryCloseEvent.Reason.PLUGIN))) {
//              Bukkit.getScheduler().runTaskLater(PaperMain.getInstance(), () -> {
//                new ProtectionShowGui(parent.getParent(), region, area, distance, regionInfo,
//                    (Player) event.getWhoClicked()).show(
//                    event.getWhoClicked());
//              }, 1);
//            }
//          }, Component.text("Grundstück erweitern", MessageManager.PRIMARY), confirmLore);

//          confirmationGui.show(player);
        });
  }

  /**
   * Returns the item for the flags
   *
   * @return The item
   */
  private GuiItem getFlagsItem() {
    List<Component> lore = new ArrayList<>();

    lore.add(Component.empty());
    lore.add(Component.text("Hier können Flags eingestellt werden", NamedTextColor.GRAY));
    lore.add(Component.empty());

    return new GuiItem(
        ItemUtils.item(Material.REDSTONE, 1, 0, Component.text("Flags", MessageManager.PRIMARY),
            lore.toArray(Component[]::new)),
        event -> new ProtectionFlagsGui(this, region).show(event.getWhoClicked()));
  }

  /**
   * Returns the item for the protection sell
   */
  @SuppressWarnings("java:S3776")
  private GuiItem getProtectionSellItem() {
    return new GuiItem(
        ItemUtils.item(Material.BEDROCK, 1, 0,
            Component.text("Grundstück verkaufen", MessageManager.PRIMARY),
            Component.empty(), Component.text("Verkauft das Grundstück", NamedTextColor.GRAY),
            Component.empty()),
        event -> {
          Player player = (Player) event.getWhoClicked();

          List<Component> confirmLore = new ArrayList<>(
              ItemUtils.splitComponent(
                  "Bist du dir sicher, dass du das Grundstück verkaufen möchtest?", 50,
                  MessageManager.ERROR));
          confirmLore.add(Component.empty());
          confirmLore.add(
              Component.text("Achtung: Das Grundstück kann nicht wiederhergestellt werden!",
                  MessageManager.ERROR));
          confirmLore.add(Component.empty());
          confirmLore
              .add(Component.text(
                  "Für das Grundstück wird dir ein Anteil des Kaufpreises erstattet.",
                  NamedTextColor.GRAY));

//          ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
//            ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
//
//            ProtectedRegion protectedRegion = regionInfo.region;
//            State canSellState = protectedRegion.getFlag(
//                ProtectionFlagsRegistry.SURF_CAN_SELL_FLAG);
//            boolean canSell = canSellState == State.ALLOW || canSellState == null;
//
//            if (!canSell) {
//              protectionUser.sendMessage(MessageManager.prefix()
//                  .append(Component.text("Das Grundstück darf nicht verkauft werden.",
//                      MessageManager.ERROR)));
//              return;
//            }
//
//            if (isRegionEdited()) {
//              protectionUser.sendMessage(MessageManager.prefix()
//                  .append(Component.text("Das Grundstück wird gerade bearbeitet!",
//                      MessageManager.ERROR)));
//              return;
//            }
//
//            BigDecimal refund = BigDecimal.valueOf(regionInfo.getRetailPrice());
//            Optional<Currency> currency = TransactionApi.getCurrency("CastCoin");
//
//            if (currency.isEmpty()) {
//              protectionUser.sendMessage(MessageManager.getNoCurrencyComponent());
//              return;
//            }
//
//            RegionManager regionManager = UtilKt.getRegionManager(player.getWorld());
//
//            if (!regionManager.hasRegion(protectedRegion.getId())) {
//              protectionUser.sendMessage(MessageManager.prefix()
//                  .append(Component.text("Das Grundstück existiert nicht mehr!",
//                      MessageManager.ERROR)));
//
//              new BukkitRunnable() {
//                @Override
//                public void run() {
//                  event.getWhoClicked().closeInventory();
//                }
//              }.runTask(PaperMain.getInstance());
//
//              return;
//            }
//
//            List<UUID> members = new ArrayList<>();
//            members.addAll(protectedRegion.getOwners().getPlayerDomain().getUniqueIds());
//            members.addAll(protectedRegion.getMembers().getPlayerDomain().getUniqueIds());
//
//            for (UUID member : members) {
//              Player memberPlayer = Bukkit.getPlayer(member);
//
//              if (memberPlayer == null || !memberPlayer.isOnline()) {
//                continue;
//              }
//
//              notifyDeletion(player, regionInfo);
//            }
//
//            // Delete the region
//            regionManager.removeRegion(protectedRegion.getId());
//
//            // Add transaction to the user
//            protectionUser.addTransaction(null, refund, currency.get(),
//                new ProtectionSellData(event.getWhoClicked().getWorld(), protectedRegion));
//
//            // Remove visualizers
//            ProtectionVisualizerThread visualizerThread = PaperMain.getBukkitInstance()
//                .getProtectionVisualizerThread();
//            visualizerThread.getVisualizers().stream()
//                .filter(protectionVisualizer -> protectionVisualizer.getRegion()
//                    .equals(protectedRegion)).findFirst()
//                .ifPresent(visualizerThread::closeVisualizer);
//
//            new BukkitRunnable() {
//              @Override
//              public void run() {
//                event.getWhoClicked().closeInventory();
//              }
//            }.runTask(PaperMain.getInstance());
//
//            protectionUser.sendMessage(
//                MessageManager.getProtectionSoldComponent(refund, currency.get()));
//          }, (clickEvent, parent) -> {
//            Bukkit.getScheduler().runTaskLater(PaperMain.getInstance(), () -> {
//              new ProtectionShowGui(parent.getParent(), region, area, distance, regionInfo,
//                  (Player) event.getWhoClicked()).show(event.getWhoClicked());
//            }, 1);
//          }, Component.text("Grundstück löschen?", MessageManager.PRIMARY), confirmLore);
//
//          confirmationGui.show(player);
        });
  }

  /**
   * Returns whether the region is edited
   *
   * @return Whether the region is edited
   */
  private boolean isRegionEdited() {
    Collection<ProtectionUser> protectionUsers = ProtectionUserManager.INSTANCE.all();
    boolean isEdited = false;

    for (ProtectionUser user : protectionUsers) {
      if (user.isCreatingRegion()) {
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
    player.sendMessage(
        MessageManager.prefix().append(Component.text("Das Grundstück ", MessageManager.INFO))
            .append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_VALUE))
            .append(Component.text(" wurde verkauft.", MessageManager.INFO)));
  }

  /**
   * @return the region
   */
  public ProtectedRegion getRegion() {
    return region;
  }
}
