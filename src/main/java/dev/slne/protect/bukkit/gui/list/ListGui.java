package dev.slne.protect.bukkit.gui.list;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.protect.bukkit.gui.PageController;
import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ListGui extends ChestGui {

	private OutlinePane backgroundPane;
	private OutlinePane backgroundPane2;
	private PaginatedPane paginatedPane;
	private StaticPane navigationPane;

	/**
	 * Creates a new protection list gui
	 */
	public ListGui(String name) {
		super(5, name);

		setOnGlobalClick(event -> event.setCancelled(true));

		ItemStack backgroundItem = ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space());

		PaginatedPane pages = new PaginatedPane(0, 1, 9, 3);
		this.paginatedPane = pages;

		OutlinePane background = new OutlinePane(0, 0, 9, 1);
		this.backgroundPane = background;
		backgroundPane.addItem(new GuiItem(backgroundItem));
		backgroundPane.setPriority(Pane.Priority.LOWEST);
		backgroundPane.setRepeat(true);

		OutlinePane background2 = new OutlinePane(0, 4, 9, 1);
		this.backgroundPane2 = background2;
		backgroundPane2.addItem(new GuiItem(backgroundItem));
		backgroundPane2.setPriority(Pane.Priority.LOWEST);
		backgroundPane2.setRepeat(true);

		StaticPane navigation = new StaticPane(0, 4, 9, 1);
		this.navigationPane = navigation;

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
		addPane(backgroundPane);
		addPane(backgroundPane2);
		addPane(navigation);
	}

	/**
	 * @return the backgroundPane
	 */
	public OutlinePane getBackgroundPane() {
		return backgroundPane;
	}

	/**
	 * @return the backgroundPane2
	 */
	public OutlinePane getBackgroundPane2() {
		return backgroundPane2;
	}

	/**
	 * @return the navigationPane
	 */
	public StaticPane getNavigationPane() {
		return navigationPane;
	}

	/**
	 * @return the paginatedPane
	 */
	public PaginatedPane getPaginatedPane() {
		return paginatedPane;
	}

}
