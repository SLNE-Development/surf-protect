package dev.slne.protect.bukkit.gui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.gui.item.ItemStackUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionListGui extends ListGui {

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
		super("Protections");

		this.regions = regions;
		this.viewingPlayer = viewingPlayer;

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

				buttons.add(new GuiItem(ItemStackUtils.getItem(Material.ACACIA_BOAT, 1, 0,
						Component.text(regionInfo.getName(), NamedTextColor.RED), lore),
						event -> {
							ProtectionListOneGui oneGui = new ProtectionListOneGui(
									region, area, finalDistance, ownersNames,
									membersNames, regionInfo, viewingPlayer);
							oneGui.show(viewingPlayer);
						}));
			}
		}

		getPaginatedPane().populateWithGuiItems(buttons);
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
