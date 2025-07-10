package dev.slne.protect.paper.gui.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.chest.SurfChestGui;
import dev.slne.protect.paper.gui.protection.ProtectionShowGui;
import dev.slne.protect.paper.gui.utils.ItemUtils;
import dev.slne.protect.paper.gui.utils.pagination.PageController;
import dev.slne.protect.paper.message.MessageManager;
import dev.slne.surf.protect.paper.region.info.RegionInfo;
import dev.slne.surf.protect.paper.util.UtilKt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class ProtectionListGui extends SurfChestGui {

  private final StaticPane navigationPane;
  private final PaginatedPane paginatedPane;
  private final List<ProtectedRegion> regions;

  private final Player viewingPlayer;

  /**
   * Creates a new protection list gui
   *
   * @param parent        the parent gui
   * @param regions       the regions to show
   * @param viewingPlayer the player viewing the gui
   */
  @SuppressWarnings("java:S3776")
  public ProtectionListGui(SurfGui parent, List<ProtectedRegion> regions,
      Player viewingPlayer) {
    super(parent, 5, Component.text("Protections - Meine Grundstücke"));

    this.viewingPlayer = viewingPlayer;

    paginatedPane = new PaginatedPane(0, 1, 9, 3);
    navigationPane = new StaticPane(0, 4, 9, 1);

    addPane(paginatedPane);
    addPane(navigationPane);

    this.regions = regions;

    update();
  }

  @Override
  public void update() {
    List<GuiItem> buttons = new ArrayList<>();

    for (ProtectedRegion region : regions) {

      List<String> ownerNames = UtilKt.getOwnerNames(region);
      List<String> memberNames = UtilKt.getMemberNames(region);

      RegionInfo regionInfo = new RegionInfo(region);
      Component none = Component.text("Keine", MessageManager.VARIABLE_VALUE);

      long distance = -1;
      Location teleportLocation = Optional.ofNullable(regionInfo.getCenterLocation()).map(BukkitAdapter::adapt).orElse(null);
      if (teleportLocation != null) {
        distance =
            viewingPlayer.getWorld().equals(teleportLocation.getWorld()) ?
                (long) viewingPlayer.getLocation().distance(teleportLocation)
                : -1;
      }

      long area = regionInfo.getVolume();

      List<Component> lore = new ArrayList<>();
      lore.add(Component.empty());
      lore.add(Component.text("Entfernung: ", NamedTextColor.GRAY));
      lore.add(Component.text(distance == -1 ? "Unbekannt" : distance + " Blöcke",
          MessageManager.VARIABLE_VALUE));
      lore.add(Component.empty());
      lore.add(Component.text("Größe: ", NamedTextColor.GRAY));
      lore.add(Component.text(area + " Blöcke", MessageManager.VARIABLE_VALUE));
      lore.add(Component.empty());

      lore.add(Component.text("Besitzer:", NamedTextColor.GRAY));
      if (ownerNames.isEmpty()) {
        lore.add(none);
      } else {
        lore.addAll(ItemUtils.splitComponent(String.join(", ", ownerNames), 50,
            MessageManager.VARIABLE_VALUE));
      }
      lore.add(Component.empty());

      lore.add(Component.text("Mitglieder:", NamedTextColor.GRAY));
      if (memberNames.isEmpty()) {
        lore.add(none);
      } else {
        lore.addAll(ItemUtils.splitComponent(String.join(", ", memberNames), 50,
            MessageManager.VARIABLE_VALUE));
      }

      lore.add(Component.empty());

      final long finalDistance = distance;

      buttons.add(new GuiItem(
          ItemUtils.item(Material.DIRT, 1, 0,
              Component.text(regionInfo.getName(), MessageManager.PRIMARY),
              lore.toArray(Component[]::new)), event -> {
        ProtectionShowGui oneGui =
            new ProtectionShowGui(this, region, area, finalDistance,
                regionInfo, (Player) event.getWhoClicked());
        oneGui.show(event.getWhoClicked());
      }));

      paginatedPane.populateWithGuiItems(buttons);
    }

    updateNavigation();
    super.update();
  }

  /**
   * Updates the navigation pane
   */
  private void updateNavigation() {
    navigationPane.addItem(
        PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", MessageManager.PRIMARY),
            paginatedPane,
            ItemUtils.paneItem()), 0, 0);
    navigationPane.addItem(
        PageController.NEXT.toGuiItem(this, Component.text("Weiter", MessageManager.PRIMARY),
            paginatedPane,
            ItemUtils.paneItem()), 8, 0);
  }

}
