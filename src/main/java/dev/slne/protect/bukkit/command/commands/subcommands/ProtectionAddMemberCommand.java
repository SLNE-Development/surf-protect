package dev.slne.protect.bukkit.command.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import net.kyori.adventure.text.Component;

public class ProtectionAddMemberCommand extends CommandAPICommand {

	public ProtectionAddMemberCommand() {
		super("addmember");

		withPermission("surf.protect.addmember");

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

		withArguments(new StringArgument("player")
				.replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream()
						.filter(target -> info.sender() instanceof Player && ((Player) info.sender()).canSee(target))
						.map(player -> player.getName()).toArray(size -> new String[size]))));

		executesPlayer((player, args) -> {

			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

			RegionInfo regionInfo = ProtectionUtils.getRegionInfo(protectionUser.getLocalPlayer(),
					(String) args.get("protectionName"));

			if (regionInfo == null) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Das Grundstück ", MessageManager.ERROR)
								.append(Component.text((String) args.get("protectionName"),
										MessageManager.VARIABLE_VALUE))
								.append(Component.text(" konnte nicht gefunden werden!", MessageManager.ERROR))));
				return;
			}

			LocalPlayer selectedLocalPlayer = ProtectionUserFinder.findLocalPlayer((String) args.get("player"));
			if (selectedLocalPlayer == null) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get("player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" konnte nicht gefunden werden!", MessageManager.ERROR))));
				return;
			}

			if (regionInfo.getRegion().getMembers().contains(selectedLocalPlayer)) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get(
										"player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" ist bereits Mitglied!", MessageManager.ERROR))));
				return;
			}

			regionInfo.getRegion().getMembers().addPlayer(selectedLocalPlayer);
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Der Spieler ", MessageManager.SUCCESS)
							.append(Component.text((String) args.get(
									"player"), MessageManager.VARIABLE_VALUE))
							.append(Component.text(" wurde hinzugefügt.", MessageManager.SUCCESS))));

		});
	}

}
