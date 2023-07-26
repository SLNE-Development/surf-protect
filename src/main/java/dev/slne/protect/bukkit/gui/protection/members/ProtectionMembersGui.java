package dev.slne.protect.bukkit.gui.protection.members;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.gui.ProtectionGui;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtectionMembersGui extends ProtectionGui {

    private final List<String> memberPlayerNames;

    /**
     * Creates a new protection members gui
     *
     * @param parent            the parent gui
     * @param memberPlayerNames the member players
     * @param viewingPlayer     the player viewing the gui
     * @param region            the region
     */
    public ProtectionMembersGui(ProtectionGui parent, List<String> memberPlayerNames, Player viewingPlayer,
                                ProtectedRegion region) {
        super(parent, 5, "Mitglieder", viewingPlayer);

        this.memberPlayerNames = memberPlayerNames;

        PaginatedPane pane = new PaginatedPane(0, 1, 9, 3);

        if (memberPlayerNames.size() > 9 * 3) {
            throw new IllegalArgumentException("Too many members");
        }

        List<GuiItem> items = new ArrayList<>();

        for (String memberName : memberPlayerNames) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberName);
            items.add(new GuiItem(ItemUtils.head(offlinePlayer,
                    Component.text(memberName, MessageManager.PRIMARY)), event -> {
                // TODO: Handle member remove
            }));
        }

        pane.populateWithGuiItems(items);

        StaticPane navigationAddition = new StaticPane(0, getRows() - 1, 9, 1);
        navigationAddition.setPriority(Pane.Priority.HIGHEST);

        navigationAddition.addItem(new GuiItem(ItemUtils.item(
                Material.PLAYER_HEAD, 1, 0, Component.text("Mitglied hinzufügen", MessageManager.PRIMARY)), event -> {
            ProtectionMemberAddAnvilGui membersGui = new ProtectionMemberAddAnvilGui(this, viewingPlayer, region);
            membersGui.show(getViewingPlayer());
        }), 1, 0);

        addPane(pane);
        addPane(navigationAddition);
    }

    /**
     * Returns the member players
     *
     * @return the member players
     */
    public List<String> getMemberPlayerNames() {
        return memberPlayerNames;
    }
}
