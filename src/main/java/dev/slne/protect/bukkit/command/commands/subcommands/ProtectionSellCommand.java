package dev.slne.protect.bukkit.command.commands.subcommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionSellCommand extends CommandAPICommand {

	private static final String PROTECTION_NAME = "protectionName2";

	public ProtectionSellCommand() {
		super("sell");

		withPermission("surf.protect.sell");

		withSubcommand(new CommandAPICommand("confirm").withArguments(new StringArgument(PROTECTION_NAME))
				.executesPlayer((player, args) -> {
					ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
					RegionInfo regionInfo = getRegionInfo(protectionUser, (String) args.get(PROTECTION_NAME));

					if (regionInfo == null) {
						protectionUser.sendMessage(MessageManager.getProtectionDoesntExistComponent());
						return;
					}

					ProtectedRegion protectedRegion = regionInfo.getRegion();
					State canSellState = protectedRegion.getFlag(ProtectionFlags.SURF_CAN_SELL_FLAG);
					boolean canSell = canSellState == State.ALLOW || canSellState == null;

					if (!canSell) {
						protectionUser.sendMessage(MessageManager.prefix()
								.append(Component.text("Das Grundstück darf nicht verkauft werden.",
										MessageManager.ERROR)));
						return;
					}

					if (protectionUser.hasRegionCreation()) {
						protectionUser.sendMessage(MessageManager.prefix()
								.append(Component.text("Das Grundstück wird gerade bearbeitet!",
										MessageManager.ERROR)));
						return;
					}

					List<String> members = new ArrayList<>();
					members.addAll(protectedRegion.getOwners().getPlayers());
					members.addAll(protectedRegion.getMembers().getPlayers());

					for (String member : members) {
						Player memberPlayer = Bukkit.getPlayer(member);

						if (!memberPlayer.isOnline())
							continue;

						notifyDeletion(player, regionInfo);
					}

					RegionManager regionManager = ProtectionUtils.getRegionManager(player.getWorld());
					regionManager.removeRegion(protectedRegion.getId());

					RegionInfo regionInfo2 = getRegionInfo(protectionUser, (String) args.get(PROTECTION_NAME));
					if (regionInfo2 == null) {
						protectionUser.addTransaction(regionInfo.getRetailPrice());
					} else {
						protectionUser.sendMessage(MessageManager.prefix()
								.append(Component.text("Fehler: Das Grundstück konnte nicht verkauft werden.",
										MessageManager.ERROR)));
					}
				}));

		withArguments(new StringArgument("protectionName").replaceSuggestions(ArgumentSuggestions.strings(info -> {
			CommandSender commandSender = info.sender();

			if (!(commandSender instanceof Player)) {
				return new String[0];
			}

			Player player = (Player) commandSender;
			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

			return ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
					.map(region -> new RegionInfo(region.getValue()).getName()).toArray(size -> new String[size]);
		})));

		executesPlayer((player, args) -> {
			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
			String protectionName = (String) args.get(PROTECTION_NAME);
			RegionInfo regionInfo = getRegionInfo(protectionUser, protectionName);
			if (regionInfo == null) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Das Grundstück existiert nicht.", MessageManager.ERROR)));
				return;
			}

			Component clickComponent = Component.text().append(Component.text("*HIER*", NamedTextColor.DARK_RED))
					.clickEvent(ClickEvent.runCommand("/protect sell " + protectionName + " confirm " + protectionName))
					.hoverEvent(HoverEvent.showText(Component
							.text("Gefährlich! Dein Grundstück kann nicht ersetzt werden.", NamedTextColor.GRAY)))
					.build();

			TextComponent.Builder sellComponentBuilder = Component.text();

			sellComponentBuilder.append(MessageManager.prefix());
			sellComponentBuilder.append(Component.text(
					"Du bist im Begriff dein Grundstück zu verkaufen. Sollte dies kein Versehen gewesen sein, klicke",
					MessageManager.WARNING));

			sellComponentBuilder.append(Component.space());
			sellComponentBuilder.append(clickComponent);
			sellComponentBuilder.append(Component.space());

			sellComponentBuilder
					.append(Component.text("und dein Grundstück wird dir für einen Anteil des Kaufpreises erstattet.",
							MessageManager.WARNING));

			player.sendMessage(sellComponentBuilder.build());
		});
	}

	private void notifyDeletion(Player player, RegionInfo regionInfo) {
		player.sendMessage(MessageManager.prefix().append(Component.text("Das Grundstück ", MessageManager.INFO))
				.append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_VALUE))
				.append(Component.text(" wurde verkauft.", MessageManager.INFO)));
	}

	private RegionInfo getRegionInfo(ProtectionUser protectionUser, String regionName) {
		return ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream().filter(region -> {
			RegionInfo regionInfoItem = new RegionInfo(region.getValue());
			return regionInfoItem != null && regionInfoItem.getName().equals(regionName);
		}).map(region -> new RegionInfo(region.getValue())).findFirst().orElse(null);
	}

}
