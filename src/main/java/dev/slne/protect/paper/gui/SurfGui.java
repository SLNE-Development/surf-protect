package dev.slne.protect.paper.gui;

import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.gui.type.util.NamedGui;
import dev.slne.protect.paper.PaperMain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SurfGui {

  /**
   * Gets the parent gui
   *
   * @return the parent gui
   */
  @Nullable
  SurfGui getParent();

  /**
   * Checks if this gui has a parent
   *
   * @return true if it has a parent, false otherwise
   */
  default boolean hasParent() {
    return getParent() != null;
  }

  /**
   * Goes back to the parent gui
   *
   * @param viewer the viewer
   */
  default void backToParent(HumanEntity viewer) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (hasParent()) {
          Gui gui = Objects.requireNonNull(getParent()).getGui();

          gui.show(viewer);
          gui.update();

          return;
        }

        viewer.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
      }
    }.runTask(PaperMain.getInstance());
  }

  /**
   * Walks the parents of this gui
   *
   * @return the parents
   */
  default @NotNull List<SurfGui> walkParents() {
    List<SurfGui> parents = new ArrayList<>();

    SurfGui parent = getParent();
    while (parent != null) {
      parents.add(parent);
      parent = parent.getParent();
    }

    return parents;
  }

  /**
   * Returns the gui
   *
   * @return the gui
   */
  @NotNull
  NamedGui getGui();
}

