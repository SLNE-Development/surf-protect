package dev.slne.protect.bukkit.gui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.gui.PageController;
import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import dev.slne.protect.bukkit.gui.protection.ProtectionShowGui;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionListGui extends ChestGui {

	private Player viewingPlayer;
	private Map<World, List<ProtectedRegion>> regions;

	/**
	 * Creates a new protection list gui
	 *
	 * @param regions       the regions to show
	 * @param viewingPlayer the player viewing the gui
	 */
	@SuppressWarnings("java:S3776")
	public ProtectionListGui(Map<World, List<ProtectedRegion>> regions, Player viewingPlayer) {
		super(5, "Flags");
		setOnGlobalClick(event -> event.setCancelled(true));

		ItemStack backgroundItem = ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space());

		List<GuiItem> buttons = new ArrayList<>();
		for (Map.Entry<World, List<ProtectedRegion>> entry : regions.entrySet()) {
			World world = entry.getKey();
			List<ProtectedRegion> worldRegions = entry.getValue();

			for (ProtectedRegion region : worldRegions) {
				RegionInfo regionInfo = new RegionInfo(world, region);
				List<LocalPlayer> owners = regionInfo.getOwners();
				List<LocalPlayer> members = regionInfo.getMembers();

				List<String> ownersNames = new ArrayList<>();
				List<String> membersNames = new ArrayList<>();

				Component none = Component.text("Keine", MessageManager.VARIABLE_VALUE);

				for (LocalPlayer owner : owners) {
					ownersNames.add(owner.getName());
				}

				for (LocalPlayer member : members) {
					membersNames.add(member.getName());
				}

				long distance = -1;
				Location teleportLocation = regionInfo.getTeleportLocation();
				if (teleportLocation != null) {
					distance = (long) (viewingPlayer.getWorld()
							.equals(teleportLocation.getWorld())
									? viewingPlayer.getLocation()
											.distance(regionInfo
													.getTeleportLocation())
									: -1);
				}

				long area = regionInfo.getArea();

				List<Component> lore = new ArrayList<>();
				lore.add(Component.empty());
				lore.add(Component.text("Entfernung: ", MessageManager.VARIABLE_KEY)
						.append(Component.text(
								distance == -1 ? "Unbekannt"
										: distance + " Blöcke",
								MessageManager.VARIABLE_VALUE)));
				lore.add(Component.empty());
				lore.add(Component.text("Größe: ", MessageManager.VARIABLE_KEY)
						.append(Component.text(area + " Blöcke",
								MessageManager.VARIABLE_VALUE)));
				lore.add(Component.empty());

				lore.add(Component.text("Besitzer:", MessageManager.VARIABLE_KEY));
				if (ownersNames.isEmpty()) {
					lore.add(none);
				} else {
					lore.addAll(ItemStackUtils.splitComponent(String.join(", ", ownersNames), 50,
							MessageManager.VARIABLE_VALUE));
				}
				lore.add(Component.empty());

				lore.add(Component.text("Mitglieder:", MessageManager.VARIABLE_KEY));
				if (membersNames.isEmpty()) {
					lore.add(none);
				} else {
					lore.addAll(ItemStackUtils.splitComponent(String.join(", ", membersNames), 50,
							MessageManager.VARIABLE_VALUE));
				}

				lore.add(Component.empty());

				final long finalDistance = distance;

				buttons.add(new GuiItem(ItemStackUtils.getItem(Material.DIRT, 1, 0,
						Component.text(regionInfo.getName(), NamedTextColor.RED), lore),
						event -> {
							ProtectionShowGui oneGui = new ProtectionShowGui(
									region, area, finalDistance, ownersNames,
									membersNames, regionInfo, viewingPlayer);
							oneGui.show(viewingPlayer);
						}));
			}
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

		addPane(background);
		addPane(background2);
		addPane(pages);
		addPane(navigation);

		this.regions = regions;
		this.viewingPlayer = viewingPlayer;
	}

	/**
	 * @return the regions
	 */
	public Map<World, List<ProtectedRegion>> getRegions() {
		return regions;
	}

	/**
	 * @return the player
	 */
	public Player getViewingPlayer() {
		return viewingPlayer;
	}
}
