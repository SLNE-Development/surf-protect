package dev.slne.protect.bukkit.gui.utils.pagination;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import java.util.function.BiPredicate;
import java.util.function.IntUnaryOperator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public enum PageController {
  PREVIOUS("MHF_ArrowLeft", (page, itemsPane) -> page > 0, page -> --page),
  NEXT("MHF_ArrowRight", (page, itemsPane) -> page < (itemsPane.getPages() - 1), page -> ++page);

  private final String skullName;
  private final BiPredicate<Integer, PaginatedPane> shouldContinue;
  private final IntUnaryOperator nextPageSupplier;

  /**
   * Creates a new page controller
   *
   * @param skullName        the skull name
   * @param shouldContinue   the predicate to check if the page should continue
   * @param nextPageSupplier the supplier for the next page
   */
  PageController(String skullName, BiPredicate<Integer, PaginatedPane> shouldContinue,
      IntUnaryOperator nextPageSupplier) {
    this.skullName = skullName;
    this.shouldContinue = shouldContinue;
    this.nextPageSupplier = nextPageSupplier;
  }

  /**
   * Converts this page controller to a gui item
   *
   * @param gui       the gui
   * @param itemName  the item name
   * @param itemsPane the items pane
   * @return the gui item
   */
  public GuiItem toGuiItem(ChestGui gui, Component itemName, PaginatedPane itemsPane,
      ItemStack failItem) {
    int currentPage = itemsPane.getPage();

    if (!this.shouldContinue.test(currentPage, itemsPane)) {
      return new GuiItem(failItem, event -> {
      });
    }

    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) item.getItemMeta();

    meta.displayName(itemName.decoration(TextDecoration.ITALIC, false));
    meta.setOwningPlayer(Bukkit.getOfflinePlayer(this.skullName));
    item.setItemMeta(meta);

    return new GuiItem(item, event -> {
      if (!this.shouldContinue.test(currentPage, itemsPane)) {
        return;
      }

      itemsPane.setPage(this.nextPageSupplier.applyAsInt(currentPage));
      gui.update();
    });
  }
}
