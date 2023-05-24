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

		withPermission("survival.protect.addmember");

		withArguments(new StringArgument("protectionName").replaceSuggestions(ArgumentSuggestions.strings(info -> {
			CommandSender commandSender = info.sender();

			if (!(commandSender instanceof Player)) {
				return new String[0];
			}

			Player player = (Player) commandSender;
			ProtectionUser customSurvivalUser = ProtectionUser.getProtectionUser(player);

			return ProtectionUtils.getRegionsFor(customSurvivalUser.getLocalPlayer()).stream()
					.map(region -> new RegionInfo(region.getValue()).getName()).toArray(size -> new String[size]);
		})));

		withArguments(new StringArgument("player")
				.replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream()
						.filter(target -> info.sender() instanceof Player && ((Player) info.sender()).canSee(target))
						.map(player -> player.getName()).toArray(size -> new String[size]))));

		executesPlayer((player, args) -> {

			ProtectionUser customSurvivalUser = ProtectionUser.getProtectionUser(player);

			RegionInfo regionInfo = ProtectionUtils.getRegionInfo(customSurvivalUser.getLocalPlayer(),
					(String) args.get("protectionName"));

			if (regionInfo == null) {
				customSurvivalUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Das Grundstück ", MessageManager.ERROR)
								.append(Component.text((String) args.get("protectionName"),
										MessageManager.VARIABLE_VALUE))
								.append(Component.text(" konnte nicht gefunden werden!", MessageManager.ERROR))));
				return;
			}

			LocalPlayer selecctedLocalPlayer = ProtectionUserFinder.findLocalPlayer((String) args.get("player"));
			if (selecctedLocalPlayer == null) {
				customSurvivalUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get("player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" konnte nicht gefunden werden!", MessageManager.ERROR))));
				return;
			}

			if (regionInfo.getRegion().getMembers().contains(selecctedLocalPlayer)) {
				customSurvivalUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get(
										"player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" ist bereits Mitglied!", MessageManager.ERROR))));
				return;
			}

			regionInfo.getRegion().getMembers().addPlayer(selecctedLocalPlayer);
			customSurvivalUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Der Spieler ", MessageManager.SUCCESS)
							.append(Component.text((String) args.get(
									"player"), MessageManager.VARIABLE_VALUE))
							.append(Component.text(" wurde hinzugefügt.", MessageManager.SUCCESS))));

		});
	}

}
