package dev.slne.protect.bukkit.command.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.utils.ProtectionInfo;
import dev.slne.protect.bukkit.utils.ProtectionSettings;
import dev.slne.protect.bukkit.utils.ProtectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ProtectionRenameCommand implements CommandExecutor, TabCompleter {

	private final int PRICE = 2500;

	public ProtectionRenameCommand(PluginCommand command) {
		command.setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!sender.hasPermission("survival.protect.rename")) {
			sender.sendMessage(MessageManager.prefix()
					.append(Component.text("Du hast keine Berechtigung für diesen Befehl.",
							MessageManager.ERROR)));
			return true;
		}

		if (args.length == 2) {
			String protectionName = args[0];
			String protectionDisplayName = args[1];

			player.sendMessage(
					MessageManager.prefix().append(Component
							.text("Um deine Region umzubenennen, klicke ",
									NamedTextColor.GRAY)
							.append(
									Component.text("hier",
											MessageManager.VARIABLE_VALUE)
											.clickEvent(ClickEvent
													.runCommand(
															"/prename " + protectionName
																	+ " "
																	+ protectionDisplayName
																	+ " confirm "
																	+ protectionName
																	+ " "
																	+ protectionDisplayName))
											.hoverEvent(HoverEvent.showText(
													Component
															.text("Klicke hier um deine Protection umzubenennen.",
																	NamedTextColor.GRAY)
															.append(Component
																	.newline())
															.append(Component
																	.newline())
															.append(Component
																	.text("ACHTUNG! ",
																			NamedTextColor.RED,
																			TextDecoration.BOLD))
															.append(Component
																	.text(
																			"Die Umbenennung kostet dich ",
																			NamedTextColor.RED))
															.append(Component
																	.text(
																			PRICE + ""/*
																						 * Currency.getDefaultCurrency()
																						 * .getCurrencySymbol(),
																						 * MessageManager.
																						 * VARIABLE_VALUE
																						 */))
															.append(Component
																	.text("!", NamedTextColor.RED)))))
							.append(Component.text(".", NamedTextColor.GRAY))));
			return true;
		} else if (args.length == 5) {
			String protectionName = args[0];
			String protectionDisplayName = args[1];
			String protectionNameConfirm = args[3];
			String protectionDisplayNameConfirm = args[4];

			if (args[2].equalsIgnoreCase("confirm")) {
				if (!protectionName.equals(protectionNameConfirm)
						|| !protectionDisplayName.equals(protectionDisplayNameConfirm)) {
					return true;
				}

				// Check if user has enough currency
				ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
				RegionInfo regionInfo = getRegionInfo(protectionUser, protectionName);

				// boolean hasEnough = user.hasEnoughCurrency(Currency.getDefaultCurrency(),
				// PRICE);
				if (true/* !hasEnough */) {
					player.sendMessage(MessageManager.prefix()
							.append(Component.text(
									"Du hast nicht genug Geld.",
									MessageManager.ERROR)));
					return true;
				}

				if (ProtectionUtils.standsInProtectedRegion(
						protectionUser.getBukkitPlayer(),
						regionInfo.getRegion())) {
					// Transaction transaction = new Transaction(null,
					// protectionUser.getBukkitPlayer(),
					// Currency.getDefaultCurrency(), -PRICE,
					// ProtectionTransactionCause.RENAMED_PROTECTION
					// .name());
					// protectionUser.getBukkitPlayer()
					// .addTransaction(transaction);
					ProtectionInfo protectionInfo = new ProtectionInfo(
							protectionDisplayName);
					regionInfo.getRegion().setFlag(
							ProtectionSettings.SURVIVAL_PROTECT_FLAG,
							protectionInfo);

					player.sendMessage(MessageManager.prefix()
							.append(Component.text(
									"Du hast die Region "
											+ protectionDisplayName
											+ " erfolgreich umbenannt.",
									MessageManager.INFO)));
					return true;
				} else {
					protectionUser.sendMessage(MessageManager.prefix()
							.append(Component.text(
									"Du befindest dich nicht auf dem zu benennenden Grundstück.",
									MessageManager.ERROR)));
					return true;
				}
			}
		}

		// MessageManager.sendCorrectUsage(sender, "/prename <protectionName>");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		List<String> suggestions = new ArrayList<String>();

		if (!(sender instanceof Player)) {
			return suggestions;
		}

		Player player = (Player) sender;
		ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

		suggestions.addAll(Arrays
				.asList(ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
						.map(region -> new RegionInfo(region.getValue()).getName())
						.toArray(size -> new String[size]))
				.stream()
				.filter(info -> info.toLowerCase().contains(args[0].toLowerCase())).toList());

		return suggestions;
	}

	private RegionInfo getRegionInfo(ProtectionUser protectionUser, String regionName) {
		RegionInfo regionInfo = ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
				.filter(region -> {
					RegionInfo regionInfoItem = new RegionInfo(region.getValue());
					return regionInfoItem != null && regionInfoItem.getName().equals(regionName);
				}).map(region -> {
					return new RegionInfo(region.getValue());
				}).findFirst().orElse(null);

		if (regionInfo == null) {
			protectionUser.sendMessage(
					Component.text("Das Grundstück existiert nicht.", MessageManager.ERROR));
			return null;
		}

		return regionInfo;
	}

}
