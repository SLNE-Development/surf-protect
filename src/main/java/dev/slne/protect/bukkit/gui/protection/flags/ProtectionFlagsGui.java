package dev.slne.protect.bukkit.gui.protection.flags;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.gui.PageController;
import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionFlagsGui extends ChestGui {

    /**
     * Creates a new protection flags gui
     *
     * @param region the region
     */
    public ProtectionFlagsGui(ProtectedRegion region) {
        super(5, "Flags");
        setOnGlobalClick(event -> event.setCancelled(true));

        ItemStack backgroundItem = ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space());

        List<GuiItem> buttons = new ArrayList<>();

        for (ProtectionFlagsMap map : ProtectionFlagsMap.values()) {
            StateFlag flag = map.getFlag();
            State state = region.getFlag(flag);

            if (state == null) {
                state = map.getInitialState();
            }

            buttons.add(new ProtectionFlagToggleButton(map.getMaterial(), map.getDisplayName(), state, v -> {
                region.setFlag(flag, State.valueOf(v));
                update();
            }));
        }

        PaginatedPane pages = new PaginatedPane(0, 1, 9, 3);
        pages.populateWithGuiItems(buttons);

        OutlinePane background = new OutlinePane(0, 0, 9, 1);
        background.addItem(new GuiItem(backgroundItem));
        background.setPriority(Pane.Priority.LOWEST);
        background.setRepeat(true);

        OutlinePane background2 = new OutlinePane(0, 4, 9, 1);
        background2.addItem(new GuiItem(backgroundItem));
        background2.setPriority(Pane.Priority.LOWEST);
        background2.setRepeat(true);

        StaticPane navigation = new StaticPane(0, 4, 9, 1);

        navigation.addItem(
                PageController.PREVIOUS.toGuiItem(this, Component.text("Zurück", NamedTextColor.GREEN), pages,
                        backgroundItem),
                0, 0);

        navigation.addItem(
                PageController.NEXT.toGuiItem(
                        this, Component.text("Weiter", NamedTextColor.GREEN), pages, backgroundItem),
                8, 0);

        navigation.addItem(
                new GuiItem(ItemStackUtils.getCloseItemStack(), event -> event.getWhoClicked().closeInventory()), 4, 0);

        addPane(pages);
        addPane(navigation);
        addPane(background);
        addPane(background2);
    }

}
