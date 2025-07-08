package dev.slne.protect.paper.gui.chest;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SurfChestGui extends ChestGui implements SurfGui {

  private final SurfGui parent;

  protected SurfChestGui(@Nullable SurfGui parent, int rows, @NotNull Component title) {
    this(parent, rows, title, true, true, true, true);
  }

  /**
   * Creates a new chest gui.
   *
   * @param parent            the parent gui
   * @param rows              the amount of rows this gui should have
   * @param title             the title of this gui
   * @param cancelBottomClick whether to cancel bottom click
   * @param cancelBottomDrag  whether to cancel bottom drag
   * @param cancelTopClick    whether to cancel top click
   * @param cancelTopDrag     whether to cancel top drag
   */
  protected SurfChestGui(@Nullable SurfGui parent, int rows, @NotNull Component title,
      boolean cancelBottomClick,
      boolean cancelBottomDrag, boolean cancelTopClick, boolean cancelTopDrag) {
    super(rows, ComponentHolder.of(title.colorIfAbsent(NamedTextColor.BLACK)));

    this.parent = parent;

    if (rows < 2) {
      throw new IllegalArgumentException("Rows must be at least 2");
    }

    setOnBottomClick(event -> event.setCancelled(cancelBottomClick));
    setOnBottomDrag(event -> event.setCancelled(cancelBottomDrag));
    setOnTopClick(event -> event.setCancelled(cancelTopClick));
    setOnTopDrag(event -> event.setCancelled(cancelTopDrag));

    addPane(GuiUtils.getOutline(0));
    addPane(GuiUtils.getOutline(rows - 1));
    addPane(GuiUtils.getNavigation(this, rows - 1));
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
