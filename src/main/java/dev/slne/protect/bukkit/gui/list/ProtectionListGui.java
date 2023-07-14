package dev.slne.protect.bukkit.gui.list;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.gui.PageController;
import dev.slne.protect.bukkit.gui.ProtectionGui;
import dev.slne.protect.bukkit.gui.protection.ProtectionShowGui;
import dev.slne.protect.bukkit.gui.utils.GuiUtils;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtectionListGui extends ProtectionGui {

    private final Map<World, List<ProtectedRegion>> regions;

    /**
     * Creates a new protection list gui
     *
     * @param parent        the parent gui
     * @param regions       the regions to show
     * @param viewingPlayer the player viewing the gui
     */
    @SuppressWarnings("java:S3776")
    public ProtectionListGui(ProtectionGui parent, Map<World, List<ProtectedRegion>> regions, Player viewingPlayer) {
        super(parent, 5, "Protections - Liste", viewingPlayer);

        List<GuiItem> buttons = new ArrayList<>();
        for (Map.Entry<World, List<ProtectedRegion>> entry : regions.entrySet()) {
            World world = entry.getKey();
            List<ProtectedRegion> worldRegions = entry.getValue();

            for (ProtectedRegion region : worldRegions) {
                RegionInfo regionInfo = new RegionInfo(world, region);
                List<LocalPlayer> owners = regionInfo.getOwners();
                List<LocalPlayer> members = regionInfo.getMembers();

                List<String> ownersNames = new ArrayList<>();
                List<String> membersNames = new ArrayList<>();

                Component none = Component.text("Keine", NamedTextColor.GOLD);

                for (LocalPlayer owner : owners) {
                    ownersNames.add(owner.getName());
                }

                for (LocalPlayer member : members) {
                    membersNames.add(member.getName());
                }

                long distance = -1;
                Location teleportLocation = regionInfo.getTeleportLocation();
                if (teleportLocation != null) {
                    distance =
                            (long) (viewingPlayer.getWorld().equals(teleportLocation.getWorld()) ? viewingPlayer.getLocation().distance(regionInfo.getTeleportLocation()) : -1);
                }

                long area = regionInfo.getArea();

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Entfernung: ", MessageManager.VARIABLE_KEY));
                lore.add(Component.text(distance == -1 ? "Unbekannt" : distance + " Blöcke", MessageManager.VARIABLE_VALUE));
                lore.add(Component.empty());
                lore.add(Component.text("Größe: ", MessageManager.VARIABLE_KEY));
                lore.add(Component.text(area + " Blöcke", MessageManager.VARIABLE_VALUE));
                lore.add(Component.empty());

                lore.add(Component.text("Besitzer:", MessageManager.VARIABLE_KEY));
                if (ownersNames.isEmpty()) {
                    lore.add(none);
                } else {
                    lore.addAll(ItemUtils.splitComponent(String.join(", ", ownersNames), 50, MessageManager.VARIABLE_VALUE));
                }
                lore.add(Component.empty());

                lore.add(Component.text("Mitglieder:", MessageManager.VARIABLE_KEY));
                if (membersNames.isEmpty()) {
                    lore.add(none);
                } else {
                    lore.addAll(ItemUtils.splitComponent(String.join(", ", membersNames), 50, MessageManager.VARIABLE_VALUE));
                }

                lore.add(Component.empty());

                final long finalDistance = distance;

                buttons.add(new GuiItem(ItemUtils.item(Material.DIRT, 1, 0, Component.text(regionInfo.getName(), NamedTextColor.GOLD), lore.toArray(Component[]::new)), event -> {
                    ProtectionShowGui oneGui =
                            new ProtectionShowGui(this, region, area, finalDistance, ownersNames, membersNames, regionInfo, viewingPlayer);
                    oneGui.show(viewingPlayer);
                }));
            }
        }

        PaginatedPane pages = new PaginatedPane(0, 1, 9, 3);
        pages.populateWithGuiItems(buttons);

        ItemStack backgroundItem = ItemUtils.paneItem();

        StaticPane navigation = new StaticPane(0, 4, 9, 1);

        navigation.addItem(PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", NamedTextColor.GOLD), pages, backgroundItem), 0, 0);
        navigation.addItem(PageController.NEXT.toGuiItem(this, Component.text("Weiter", NamedTextColor.GOLD), pages, backgroundItem), 8, 0);

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(4));
        addPane(pages);
        addPane(navigation);

        this.regions = regions;

        update();
    }

    /**
     * @return the regions
     */
    public Map<World, List<ProtectedRegion>> getRegions() {
        return regions;
    }

}
