package dev.slne.protect.bukkit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import dev.slne.protect.bukkit.gui.list.ProtectionListGui;
import dev.slne.protect.bukkit.gui.utils.ConfirmationGui;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionGui extends ChestGui {

	private Player targetProtectionPlayer;

	/**
	 * Creates a new protection gui
	 *
	 * @param targetProtectionPlayer the player get the regions from
	 */
	public ProtectionGui(Player viewingPlayer, Player targetProtectionPlayer) {
		super(5, "Protections");

		setOnGlobalClick(event -> event.setCancelled(true));

		this.targetProtectionPlayer = targetProtectionPlayer;

		OutlinePane backgroundPane = new OutlinePane(0, 0, 9, 1);
		backgroundPane.addItem(new GuiItem(
				ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space())));
		backgroundPane.setRepeat(true);
		backgroundPane.setPriority(Pane.Priority.LOWEST);

		OutlinePane backgroundPane2 = new OutlinePane(0, 4, 9, 1);
		backgroundPane2.addItem(new GuiItem(
				ItemStackUtils.getItem(Material.BLACK_STAINED_GLASS_PANE, 1, 0, Component.space())));
		backgroundPane2.setRepeat(true);
		backgroundPane2.setPriority(Pane.Priority.LOWEST);

		StaticPane navigationPane = new StaticPane(0, 0, 9, 5);

		if (viewingPlayer.hasPermission("surf.protect.list")) {
			navigationPane.addItem(getProtectionListItem(), 1, 2);
		}

		if (viewingPlayer.hasPermission("surf.protect.visualize")) {
			navigationPane.addItem(getVisualizerItem(), 2, 2);
		}

		if (viewingPlayer.hasPermission("surf.protect.create")) {
			navigationPane.addItem(getProtectionCreateItem(), 7, 2);
		}

		navigationPane.addItem(
				new GuiItem(ItemStackUtils.getCloseItemStack(), event -> event.getWhoClicked().closeInventory()), 4, 4);

		addPane(backgroundPane);
		addPane(backgroundPane2);
		addPane(navigationPane);
	}

	/**
	 * Returns the protection create item
	 *
	 * @return the protection create item
	 */
	private GuiItem getProtectionCreateItem() {
		return new GuiItem(
				ItemStackUtils.getItem(Material.GRASS_BLOCK, 1, 0,
						Component.text("Grundstück erstellen", MessageManager.VARIABLE_KEY), Component.empty(),
						Component.text("Erstelle ein neues Grundstück", NamedTextColor.GRAY), Component.empty()),
				event -> {
					Player player = (Player) event.getWhoClicked();

					List<Component> confirmLore = new ArrayList<>();
					confirmLore.add(Component.empty());
					confirmLore
							.addAll(ItemStackUtils.splitComponent(
									"Bist du dir sicher, dass du ein Grundstück erstellen möchtest?",
									50, MessageManager.VARIABLE_VALUE));

					ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
						ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
						ProtectionRegion regionCreation = new ProtectionRegion(protectionUser, null);
						protectionUser.startRegionCreation(regionCreation);

						MessageManager.sendProtectionModeEnterMessages(protectionUser);

						new BukkitRunnable() {
							@Override
							public void run() {
								confirmEvent.getWhoClicked().closeInventory();
							}
						}.runTaskLater(BukkitMain.getInstance(), 1);
					}, cancelEvent -> {

					}, Component.text("Grundstück erweitern", MessageManager.VARIABLE_KEY), confirmLore);

					confirmationGui.show(player);
				});
	}

	/**
	 * Gets the visualizer item
	 *
	 * @return the visualizer item
	 */
	private GuiItem getVisualizerItem() {
		return new GuiItem(ItemStackUtils.getItem(Material.ENDER_EYE, 1, 0,
				Component.text("Visualizier", MessageManager.VARIABLE_KEY), Component.empty(),
				Component.text("Aktiviert/Deaktiviert den Visualizer", NamedTextColor.GRAY), Component.empty()),
				event -> {
					Player player = (Player) event.getWhoClicked();
					List<ProtectedRegion> regions = ProtectionUtils.getRegionManager(player.getWorld()).getRegions()
							.values().stream().filter(region -> !region.getType().equals(RegionType.GLOBAL)).toList();

					boolean state = BukkitMain.getBukkitInstance().getProtectionVisualizerState()
							.getPlayerState(player);

					if (!state) {
						for (ProtectedRegion region : regions) {
							State visualizeState = region.getFlag(ProtectionFlags.SURF_PROTECT_VISUALIZE);

							if (visualizeState != null && visualizeState.equals(State.DENY)) {
								continue;
							}

							BukkitMain.getBukkitInstance().getProtectionVisualizerThread().addVisualizer(
									player.getWorld(), region, player);
						}
					} else {
						for (ProtectionVisualizer<?> visualizer : new ArrayList<>(
								BukkitMain.getBukkitInstance().getProtectionVisualizerThread()
										.getVisualizers(player))) {
							visualizer.remove();
						}

						BukkitMain.getBukkitInstance().getProtectionVisualizerThread().removeVisualizers(player);
					}

					player.sendMessage(MessageManager.getProtectionVisualizeComponent(!state));
					BukkitMain.getBukkitInstance().getProtectionVisualizerState().togglePlayerState(player);

					player.closeInventory();
				});
	}

	/**
	 * Gets the protection list item
	 *
	 * @return the protection list item
	 */
	private GuiItem getProtectionListItem() {
		return new GuiItem(
				ItemStackUtils.getItem(Material.DIRT, 1, 0,
						Component.text("Liste", MessageManager.VARIABLE_KEY),
						Component.text("Eine Liste von Protections", NamedTextColor.GRAY)),
				event -> {
					Player viewingPlayer = (Player) event.getWhoClicked();
					ProtectionUser user = ProtectionUser.getProtectionUser(getTargetProtectionPlayer());
					Map<World, List<ProtectedRegion>> regions = ProtectionUtils
							.getRegionListFor(user.getLocalPlayer());

					ProtectionListGui listGui = new ProtectionListGui(regions, viewingPlayer);
					listGui.show(viewingPlayer);
				});
	}

	/**
	 * @return the targetProtectionPlayer
	 */
	public Player getTargetProtectionPlayer() {
		return targetProtectionPlayer;
	}
}
