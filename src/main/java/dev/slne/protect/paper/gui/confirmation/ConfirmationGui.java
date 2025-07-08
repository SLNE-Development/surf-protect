package dev.slne.protect.paper.gui.confirmation;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.utils.GuiUtils;
import dev.slne.protect.paper.gui.utils.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConfirmationGui extends ChestGui implements SurfGui {

  private final SurfGui parent;

  private final Consumer<InventoryClickEvent> onConfirm;
  private final BiConsumer<InventoryEvent, SurfGui> onCancel;

  /**
   * Creates a new confirmation gui.
   *
   * @param previousGui   the gui to return to when the player cancels
   * @param onConfirm     the action to perform when the player confirms
   * @param onCancel      the action to perform when the player cancels
   * @param questionLabel the label of the question
   * @param questionLore  the lore of the question
   */
  public ConfirmationGui(SurfGui previousGui, Consumer<InventoryClickEvent> onConfirm,
      BiConsumer<InventoryEvent, SurfGui> onCancel, Component questionLabel, List<Component> questionLore) {
    super(5, "Bestätigung erforderlich");

    this.parent = previousGui;

    this.onConfirm = onConfirm;
    this.onCancel = onCancel;

    setOnGlobalClick(event -> event.setCancelled(true));

    setOnClose(event -> {
      if (!event.getReason().equals(InventoryCloseEvent.Reason.PLUGIN)) {
        cancel(event);
      }
    });

    StaticPane confirmationPane = new StaticPane(0, 0, 9, 5);

    confirmationPane.addItem(new GuiItem(ItemUtils.confirmationConfirmItem(), this::confirm), 1, 2);
    confirmationPane.addItem(new GuiItem(ItemUtils.confirmationCancelItem(), this::cancel), 7, 2);

    List<Component> lore = new ArrayList<>();
    lore.add(Component.empty());
    lore.add(Component.text("Es ist eine Bestätigung erforderlich...", NamedTextColor.GRAY));
    lore.add(Component.empty());
    lore.addAll(questionLore);
    lore.add(Component.empty());

    confirmationPane.addItem(new GuiItem(
            ItemUtils.confirmationQuestionItem(questionLabel,
                lore.toArray(Component[]::new))),
        4, 2);

    addPane(GuiUtils.getOutline(0));
    addPane(GuiUtils.getOutline(4));
    addPane(confirmationPane);
  }

  /**
   * Cancels the action
   *
   * @param event the event
   */
  public void cancel(InventoryEvent event) {
    if (onCancel != null) {
      onCancel.accept(event, parent);
    }
  }

  /**
   * Confirms the action
   *
   * @param event the event
   */
  public void confirm(InventoryClickEvent event) {
    if (onConfirm != null) {
      onConfirm.accept(event);
    }
  }

  @Override
  public @Nullable SurfGui getParent() {
    return parent;
  }

  @Override
  public @NotNull NamedGui getGui() {
    return this;
  }

}

