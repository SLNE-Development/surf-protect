package dev.slne.protect.bukkit.gui.protection.members;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import dev.slne.surf.gui.api.SurfGui;
import dev.slne.surf.gui.api.chest.SurfChestGui;
import dev.slne.surf.gui.api.confirmation.ConfirmationGui;
import dev.slne.surf.gui.api.utils.ItemUtils;
import dev.slne.surf.gui.api.utils.pagination.PageController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtectionMembersGui extends SurfChestGui {

    private final StaticPane navigationPane;
    private final PaginatedPane paginatedPane;
    private final ProtectedRegion region;

    /**
     * Creates a new protection members gui
     *
     * @param parent        the parent gui
     * @param viewingPlayer the player viewing the gui
     * @param region        the region
     */
    public ProtectionMembersGui(SurfGui parent, Player viewingPlayer, ProtectedRegion region) {
        super(parent, 5, Component.text("Mitglieder"), viewingPlayer);

        this.region = region;

        paginatedPane = new PaginatedPane(0, 1, 9, 3);

        navigationPane = new StaticPane(0, getRows() - 1, 9, 1);
        navigationPane.setPriority(Pane.Priority.HIGHEST);

        navigationPane.addItem(new GuiItem(ItemUtils.item(
                Material.PLAYER_HEAD, 1, 0, Component.text("Mitglied hinzufügen", MessageManager.PRIMARY),
                Component.empty(), Component.text("Klicke hier, um ein Mitglied hinzuzufügen",
                        NamedTextColor.GRAY), Component.empty()),
                event -> {
                    ProtectionMemberAddAnvilGui membersGui =
                            new ProtectionMemberAddAnvilGui(this, viewingPlayer, region);
                    membersGui.show(getViewingPlayer());
                }), 1, 0);

        addPane(paginatedPane);
        addPane(navigationPane);

        update();
    }

    /**
     * Gets the region
     *
     * @return the region
     */
    public ProtectedRegion getRegion() {
        return region;
    }

    @Override
    public void update() {
        List<GuiItem> items = new ArrayList<>();
        List<String> memberNames = ProtectionUtils.getMemberNames(region);

        for (String memberName : memberNames) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberName);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Bitte bestätige, dass du ", MessageManager.ERROR)
                    .append(Component.text(memberName, MessageManager.VARIABLE_VALUE)).append(Component.text(" " +
                            "aus der", MessageManager.ERROR)));
            lore.add(Component.text("Region entfernen möchtest.", MessageManager.ERROR));

            List<Component> itemLore = new ArrayList<>();
            itemLore.add(Component.empty());
            itemLore.add(Component.text("Klicke hier, um ", NamedTextColor.GRAY)
                    .append(Component.text(memberName, MessageManager.VARIABLE_VALUE)).append(Component.text(" " +
                            "aus der", NamedTextColor.GRAY)));
            itemLore.add(Component.text("Region zu entfernen", NamedTextColor.GRAY));
            itemLore.add(Component.empty());

            items.add(new GuiItem(ItemUtils.head(offlinePlayer,
                    Component.text(memberName, MessageManager.PRIMARY), itemLore.toArray(Component[]::new)),
                    event -> {
                        new ConfirmationGui(this, getViewingPlayer(), clickEvent -> {
                            LocalPlayer localPlayer = ProtectionUserFinder.findLocalPlayer(memberName);
                            region.getMembers().removePlayer(localPlayer);

                            new ProtectionMembersGui(getParent(), getViewingPlayer(), region).show(
                                    getViewingPlayer());
                        }, clickEvent -> {
                            new ProtectionMembersGui(getParent(), getViewingPlayer(), region).show(
                                    getViewingPlayer());
                        }, Component.text("Bestätigung erforderlich", MessageManager.PRIMARY), lore).show(
                                getViewingPlayer());
                    }));
        }

        navigationPane.addItem(
                PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", MessageManager.PRIMARY), paginatedPane,
                        ItemUtils.paneItem()), 0, 0);
        navigationPane.addItem(
                PageController.NEXT.toGuiItem(this, Component.text("Weiter", MessageManager.PRIMARY), paginatedPane,
                        ItemUtils.paneItem()), 8, 0);

        paginatedPane.populateWithGuiItems(items);
        ProtectionMembersGui.super.update();
    }
}
