package dev.slne.protect.bukkitold.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import dev.slne.protect.bukkitold.BukkitMain;
import dev.slne.protect.bukkitold.gui.chest.SurfChestGui;
import dev.slne.protect.bukkitold.gui.confirmation.ConfirmationGui;
import dev.slne.protect.bukkitold.gui.list.ProtectionListGui;
import dev.slne.protect.bukkitold.gui.utils.ItemUtils;
import dev.slne.protect.bukkitold.message.MessageManager;
import dev.slne.protect.bukkitold.region.ProtectionRegion;
import dev.slne.protect.bukkitold.region.ProtectionUtils;
import dev.slne.protect.bukkitold.region.visual.visualizer.ProtectionVisualizer;
import dev.slne.protect.bukkitold.user.ProtectionUser;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ProtectionMainMenu extends SurfChestGui {


  private final OfflinePlayer targetProtectionPlayer;

  /**
   * Creates a new protection gui
   *
   * @param targetProtectionPlayer the player get the regions from
   */
  public ProtectionMainMenu(Player viewingPlayer, OfflinePlayer targetProtectionPlayer) {
    super(null, 5, Component.text("Protections - Menü"));

    this.targetProtectionPlayer = targetProtectionPlayer;

    StaticPane navigationPane = new StaticPane(0, 0, 9, 5);

    if (viewingPlayer.hasPermission("surf.protect.list")) {
      navigationPane.addItem(getProtectionListItem(), 2, 2);
    }

    if (viewingPlayer.hasPermission("surf.protect.visualize")) {
      navigationPane.addItem(getVisualizerItem(), 7, 2);
    }

    if (viewingPlayer.hasPermission("surf.protect.create")) {
      navigationPane.addItem(getProtectionCreateItem(), 1, 2);
    }

    if (viewingPlayer.hasPermission("surf.protect.plotmessages")) {
      navigationPane.addItem(getPlotMessagesItem(), 6, 2);
    }

    addPane(navigationPane);
  }

  private GuiItem getPlotMessagesItem() {
    return new GuiItem(
        ItemUtils.item(Material.LEATHER_BOOTS, 1, 0,
            Component.text("Grundstück Nachrichten", MessageManager.PRIMARY),
            Component.empty(),
            Component.text("Aktiviert/Deaktiviert die Nachrichten", NamedTextColor.GRAY),
            Component.text("beim Betreten/Verlassen eines Grundstücks", NamedTextColor.GRAY),
            Component.empty()),
        event -> {
          final HumanEntity clicked = event.getWhoClicked();
          final PersistentDataContainer pdc = clicked.getPersistentDataContainer();
          final boolean newState = !pdc.getOrDefault(PLOT_MESSAGES_KEY, PersistentDataType.BOOLEAN,
              DEFAULT_PLOT_MESSAGES);

          pdc.set(PLOT_MESSAGES_KEY, PersistentDataType.BOOLEAN, newState);
          clicked.sendMessage(MessageManager.getPlotMessagesChangedComponent(newState));
          clicked.closeInventory();
        }
    );
  }

  /**
   * Gets the protection list item
   *
   * @return the protection list item
   */
  private GuiItem getProtectionListItem() {
    return new GuiItem(
        ItemUtils.item(Material.DIRT, 1, 0,
            Component.text("Meine Grundstücke", MessageManager.PRIMARY),
            Component.empty(),
            Component.text("Eine Liste mit allen deinen Grundstücken", NamedTextColor.GRAY),
            Component.empty()),
        event -> {
          Player viewingPlayer = (Player) event.getWhoClicked();
          ProtectionUser user = ProtectionUser.getProtectionUser(getTargetProtectionPlayer());
          Map<World, List<ProtectedRegion>> regions = ProtectionUtils.getRegionListFor(
              user.getLocalPlayer());

          ProtectionListGui listGui = new ProtectionListGui(this, regions, viewingPlayer);
          listGui.show(viewingPlayer);
        });
  }

  /**
   * Gets the visualizer item
   *
   * @return the visualizer item
   */
  private GuiItem getVisualizerItem() {
    return new GuiItem(
        ItemUtils.item(Material.ENDER_EYE, 1, 0,
            Component.text("Visualizier", MessageManager.PRIMARY),
            Component.empty(),
            Component.text("Aktiviert/Deaktiviert den Visualizer", NamedTextColor.GRAY),
            Component.empty()), event -> {
      Player player = (Player) event.getWhoClicked();
      player.closeInventory();

      List<ProtectedRegion> regions =
          ProtectionUtils.getRegionManager(player.getWorld()).getRegions().values().stream()
              .filter(region -> !region.getType().equals(RegionType.GLOBAL)).toList();

      boolean state = BukkitMain.getBukkitInstance().getProtectionVisualizerState()
          .getPlayerState(player);

      if (!state) {
        for (ProtectedRegion region : regions) {
          State visualizeState = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_VISUALIZE);

          if (visualizeState != null && visualizeState.equals(State.DENY)) {
            continue;
          }

          BukkitMain.getBukkitInstance().getProtectionVisualizerThread()
              .addVisualizer(player.getWorld(), region, player);
        }
      } else {
        for (ProtectionVisualizer<?> visualizer : new ArrayList<>(
            BukkitMain.getBukkitInstance().getProtectionVisualizerThread()
                .getVisualizers(player))) {
          visualizer.remove();
        }

        BukkitMain.getBukkitInstance().getProtectionVisualizerThread().removeVisualizers(player);
      }

      player.sendMessage(MessageManager.getProtectionVisualizeComponent(!state));

      Duration fadeDuration = Duration.ofMillis(150);
      Duration stayDuration = Duration.ofSeconds(1);

      player.showTitle(Title.title(Component.text("Visualizer", MessageManager.PRIMARY),
          Component.text("Visualizer " + (state ? "deaktiviert" : "aktiviert"),
              NamedTextColor.GRAY),
          Title.Times.times(fadeDuration, stayDuration, fadeDuration)));
      BukkitMain.getBukkitInstance().getProtectionVisualizerState().togglePlayerState(player);
    });
  }

  /**
   * Returns the protection create item
   *
   * @return the protection create item
   */
  private GuiItem getProtectionCreateItem() {
    return new GuiItem(
        ItemUtils.item(Material.GRASS_BLOCK, 1, 0,
            Component.text("Grundstück erstellen", MessageManager.PRIMARY),
            Component.empty(), Component.text("Erstelle ein neues Grundstück", NamedTextColor.GRAY),
            Component.empty()), event -> {
      Player player = (Player) event.getWhoClicked();

      List<Component> confirmLore = new ArrayList<>(
          ItemUtils.splitComponent("Bist du dir sicher, dass du ein Grundstück erstellen möchtest?",
              50, NamedTextColor.GRAY));

      ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
        ProtectionRegion regionCreation = new ProtectionRegion(protectionUser, null);

        confirmEvent.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        protectionUser.startRegionCreation(regionCreation);
      }, cancelEvent -> {
        if (!(cancelEvent instanceof InventoryCloseEvent closeEvent && closeEvent.getReason()
            .equals(
                InventoryCloseEvent.Reason.PLUGIN))) {
          new ProtectionMainMenu(player, getTargetProtectionPlayer()).show(player);
        }
      }, Component.text("Grundstück erstellen", MessageManager.PRIMARY), confirmLore);

      confirmationGui.show(player);
    });
  }

  /**
   * @return the targetProtectionPlayer
   */
  public OfflinePlayer getTargetProtectionPlayer() {
    return targetProtectionPlayer;
  }
}
