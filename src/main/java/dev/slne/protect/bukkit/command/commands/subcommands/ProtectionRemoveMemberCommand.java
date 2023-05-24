package dev.slne.protect.bukkit.command.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import net.kyori.adventure.text.Component;

public class ProtectionRemoveMemberCommand extends CommandAPICommand {

	public ProtectionRemoveMemberCommand() {
		super("removemember");

		withPermission("survival.protect.removemember");

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

		withArguments(new StringArgument("player").replaceSuggestions(ArgumentSuggestions.strings(info -> {
			CommandSender commandSender = info.sender();

			if (!(commandSender instanceof Player)) {
				return new String[0];
			}

			Player player = (Player) commandSender;
			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

			String regionName = (String) info.previousArgs().get("protectionName");
			RegionInfo regionInfo = ProtectionUtils.getRegionInfo(protectionUser.getLocalPlayer(), regionName);

			if (regionInfo == null) {
				return new String[0];
			}

			ProfileCache cache = WorldGuard.getInstance().getProfileCache();

			return regionInfo.getMembers().stream().map(member -> {
				String userName = member.getName();
				if (userName == null) {
					Profile profile = cache.getIfPresent(member.getUniqueId());

					if (profile == null) {
						return null;
					}

					userName = profile.getName();
				}

				return userName;
			}).toArray(size -> new String[size]);
		})));

		executesPlayer((player, args) -> {

			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

			RegionInfo regionInfo = ProtectionUtils.getRegionInfo(protectionUser.getLocalPlayer(),
					(String) args.get("protectionName"));

			if (regionInfo == null) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Das Grundstück ", MessageManager.ERROR)
								.append(Component.text((String) args.get(
										"protectionName"), MessageManager.VARIABLE_VALUE))
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

			if (!regionInfo.getRegion().getMembers().contains(selectedLocalPlayer)) {
				protectionUser.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get("player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" ist kein Mitglied!", MessageManager.ERROR))));
				return;
			}

			regionInfo.getRegion().getMembers().removePlayer(selectedLocalPlayer);
			protectionUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Der Spieler ", MessageManager.SUCCESS)
							.append(Component.text((String) args.get(
									"player"), MessageManager.VARIABLE_VALUE))
							.append(Component.text(" wurde entfernt.", MessageManager.SUCCESS))));

		});
	}

}
