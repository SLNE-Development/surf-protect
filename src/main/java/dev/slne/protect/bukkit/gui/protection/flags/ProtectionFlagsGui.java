package dev.slne.protect.bukkit.gui.protection.flags;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.gui.PageController;
import dev.slne.protect.bukkit.gui.ProtectionGui;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ProtectionFlagsGui extends ProtectionGui {

    /**
     * Creates a new protection flags gui
     *
     * @param region the region
     */
    public ProtectionFlagsGui(ProtectionGui parent, ProtectedRegion region, Player viewingPlayer) {
        super(parent, 5, parent.getTitle() + " - Flags", viewingPlayer);

        List<GuiItem> buttons = new ArrayList<>();

        for (ProtectionFlagsMap map : ProtectionFlagsMap.values()) {
            StateFlag flag = map.getFlag();
            State state = region.getFlag(flag);

            buttons.add(new ToggleButton(map.getMaterial(), map.getDisplayName(), state, map.getToggleToState(), v -> {
                region.setFlag(flag, v);
                update();
            }));
        }

        PaginatedPane pages = new PaginatedPane(0, 1, 9, 3);
        pages.populateWithGuiItems(buttons);

        StaticPane navigation = new StaticPane(0, 4, 9, 1);

        ItemStack backgroundItem = ItemUtils.paneItem();
        navigation.addItem(PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", NamedTextColor.GREEN), pages, backgroundItem), 0, 0);
        navigation.addItem(PageController.NEXT.toGuiItem(this, Component.text("Weiter", NamedTextColor.GREEN), pages, backgroundItem), 8, 0);

        addPane(pages);
        addPane(navigation);
    }

}
