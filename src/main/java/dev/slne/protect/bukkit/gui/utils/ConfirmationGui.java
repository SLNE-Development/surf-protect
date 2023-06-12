package dev.slne.protect.bukkit.gui.utils;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ConfirmationGui extends ChestGui {

    private Gui previousGui;

    private Component questionLabel;
    private List<Component> questionLore;
    private Consumer<InventoryClickEvent> onConfirm;
    private Consumer<InventoryEvent> onCancel;

    /**
     * Creates a new confirmation gui
     *
     * @param previousGui the previous gui
     * @param onConfirm   the action when the user confirms
     * @param onCancel    the action when the user cancels
     */
    public ConfirmationGui(Gui previousGui, Consumer<InventoryClickEvent> onConfirm,
            Consumer<InventoryEvent> onCancel, Component questionLabel, List<Component> questionLore) {
        super(5, "Bestätigung");

        this.previousGui = previousGui;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.questionLabel = questionLabel;
        this.questionLore = questionLore;

        setOnGlobalClick(event -> event.setCancelled(true));

        setOnClose(event -> cancel(event, (Player) event.getPlayer()));

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

        StaticPane confirmationPane = new StaticPane(0, 0, 9, 5);

        confirmationPane.addItem(new GuiItem(ItemStackUtils.getItem(Material.GREEN_CONCRETE, 1, 0,
                Component.text("Bestätigen", NamedTextColor.GREEN)), this::confirm), 1, 2);

        confirmationPane.addItem(new GuiItem(ItemStackUtils.getItem(Material.RED_CONCRETE, 1, 0,
                Component.text("Abbrechen", NamedTextColor.RED)),
                event -> cancel(event, (Player) event.getWhoClicked())), 7, 2);

        confirmationPane.addItem(
                new GuiItem(ItemStackUtils.getItem(Material.ENCHANTED_BOOK, 1, 0, questionLabel, questionLore)), 4, 2);

        addPane(backgroundPane);
        addPane(backgroundPane2);
        addPane(confirmationPane);
    }

    /**
     * Confirms the action
     *
     * @param event the event
     */
    public void confirm(InventoryClickEvent event) {
        onConfirm.accept(event);
    }

    /**
     * Cancels the action
     *
     * @param event  the event
     * @param player the player
     */
    public void cancel(InventoryEvent event, Player player) {
        onCancel.accept(event);
        previousGui.show(player);
        previousGui.update();
    }

    /**
     * @return the onCancel
     */
    public Consumer<InventoryEvent> getOnCancel() {
        return onCancel;
    }

    /**
     * @return the onConfirm
     */
    public Consumer<InventoryClickEvent> getOnConfirm() {
        return onConfirm;
    }

    /**
     * @return the previousGui
     */
    public Gui getPreviousGui() {
        return previousGui;
    }

    /**
     * @return the questionLabel
     */
    public Component getQuestionLabel() {
        return questionLabel;
    }

    /**
     * @return the questionLore
     */
    public List<Component> getQuestionLore() {
        return questionLore;
    }

}
