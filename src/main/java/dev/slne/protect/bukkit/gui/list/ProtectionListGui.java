package dev.slne.protect.bukkit.gui.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.protection.ProtectionShowGui;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.surf.gui.api.SurfGui;
import dev.slne.surf.gui.api.chest.SurfChestGui;
import dev.slne.surf.gui.api.utils.ItemUtils;
import dev.slne.surf.gui.api.utils.pagination.PageController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProtectionListGui extends SurfChestGui {

    private final StaticPane navigationPane;
    private final PaginatedPane paginatedPane;
    private final Map<World, List<ProtectedRegion>> regions;

    /**
     * Creates a new protection list gui
     *
     * @param parent        the parent gui
     * @param regions       the regions to show
     * @param viewingPlayer the player viewing the gui
     */
    @SuppressWarnings("java:S3776")
    public ProtectionListGui(SurfGui parent, Map<World, List<ProtectedRegion>> regions, Player viewingPlayer) {
        super(parent, 5, Component.text("Protections - Liste"), viewingPlayer);

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

        for (Map.Entry<World, List<ProtectedRegion>> entry : regions.entrySet()) {
            World world = entry.getKey();
            List<ProtectedRegion> worldRegions = entry.getValue();

            for (ProtectedRegion region : worldRegions) {
                CompletableFuture<List<String>> ownerNamesFuture =
                        ProtectionUtils.getOwnerNames(region).exceptionally(exception -> {
                            exception.printStackTrace();
                            return null;
                        });
                CompletableFuture<List<String>> memberNamesFuture =
                        ProtectionUtils.getMemberNames(region).exceptionally(exception -> {
                            exception.printStackTrace();
                            return null;
                        });

                CompletableFuture.allOf(ownerNamesFuture, memberNamesFuture).thenAcceptAsync(v -> {
                    List<String> ownersNames = ownerNamesFuture.join();
                    List<String> membersNames = memberNamesFuture.join();

                    RegionInfo regionInfo = new RegionInfo(world, region);
                    Component none = Component.text("Keine", MessageManager.VARIABLE_VALUE);

                    long distance = -1;
                    Location teleportLocation = regionInfo.getTeleportLocation();
                    if (teleportLocation != null) {
                        distance =
                                (long) (getViewingPlayer().getWorld().equals(teleportLocation.getWorld()) ?
                                        getViewingPlayer().getLocation().distance(regionInfo.getTeleportLocation()) :
                                        -1);
                    }

                    long area = regionInfo.getArea();

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
                    if (ownersNames.isEmpty()) {
                        lore.add(none);
                    } else {
                        lore.addAll(ItemUtils.splitComponent(String.join(", ", ownersNames), 50,
                                MessageManager.VARIABLE_VALUE));
                    }
                    lore.add(Component.empty());

                    lore.add(Component.text("Mitglieder:", NamedTextColor.GRAY));
                    if (membersNames.isEmpty()) {
                        lore.add(none);
                    } else {
                        lore.addAll(ItemUtils.splitComponent(String.join(", ", membersNames), 50,
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
                                        regionInfo, getViewingPlayer());
                        oneGui.show(getViewingPlayer());
                    }));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            paginatedPane.populateWithGuiItems(buttons);
                            updateNavigation();
                            ProtectionListGui.super.update();
                        }
                    }.runTask(BukkitMain.getInstance());
                }).exceptionally(exception -> {
                    exception.printStackTrace();
                    return null;
                });
            }
        }

        updateNavigation();
        super.update();
    }

    /**
     * Updates the navigation pane
     */
    private void updateNavigation() {
        ItemStack backgroundItem = ItemUtils.paneItem();
        navigationPane.addItem(
                PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", MessageManager.PRIMARY), paginatedPane,
                        backgroundItem), 0, 0);
        navigationPane.addItem(
                PageController.NEXT.toGuiItem(this, Component.text("Weiter", MessageManager.PRIMARY), paginatedPane,
                        backgroundItem), 8, 0);
    }

    /**
     * @return the regions
     */
    public Map<World, List<ProtectedRegion>> getRegions() {
        return regions;
    }

}
