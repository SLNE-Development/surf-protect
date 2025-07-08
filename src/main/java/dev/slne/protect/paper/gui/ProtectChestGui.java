package dev.slne.protect.paper.gui;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import dev.slne.protect.paper.PaperMain;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * The type Protect chest gui.
 */
public class ProtectChestGui extends ChestGui {

  private Gui parent;

  /**
   * Instantiates a new Protect chest gui.
   *
   * @param rows  the rows
   * @param title the title
   */
  public ProtectChestGui(int rows, Component title) {
    super(rows, ComponentHolder.of(title));
  }

  @Override
  public void setParent(@NotNull Gui gui) {
    this.parent = gui;
  }

  /**
   * Gets parent.
   *
   * @return the parent
   */
  public Gui getParent() {
    return parent;
  }

  /**
   * Has parent boolean.
   *
   * @return the boolean
   */
  public boolean hasParent() {
    return parent != null;
  }

  /**
   * Back to parent.
   *
   * @param humanEntity the human entity
   */
  public void backToParent(HumanEntity humanEntity) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (hasParent()) {
          Gui gui = getParent();

          gui.show(humanEntity);
          gui.update();

          return;
        }

        humanEntity.closeInventory();
      }
    }.runTask(PaperMain.getInstance());
  }

  @Override
  public void navigateToParent(@NotNull HumanEntity humanEntity) {
    throw new UnsupportedOperationException(
        "This method is rsemoved as inventoryframework is calling this method automatically, without any advice when the gui is closed...");
  }

  /**
   * Walk parents list.
   *
   * @return the list
   */
  public List<ProtectChestGui> walkParents() {
    List<ProtectChestGui> parents = new ArrayList<>();

    if (!(getParent() instanceof ProtectChestGui protectParent)) {
      return parents;
    }

    while (this.parent != null) {
      parents.add(protectParent);
      this.parent = protectParent.getParent();
    }

    return parents;
  }

  /**
   * Gets gui.
   *
   * @return the gui
   */
  public NamedGui getGui() {
    return this;
  }
}
