package dev.slne.protect.bukkit.command.commands.subcommands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.util.profile.Profile;
import com.sk89q.worldguard.util.profile.cache.ProfileCache;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ProtectionInfoCommand extends CommandAPICommand {

	public ProtectionInfoCommand() {
		super("info");

		withPermission("surf.protect.info");

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
			String protectionName = (String) args.get("protectionName");
			RegionInfo regionInfo = ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
					.map(region -> new RegionInfo(region.getValue())).filter(info -> info.getName()
							.equals(protectionName))
					.findFirst().orElse(null);

			if (regionInfo == null) {
				protectionUser.sendMessage(Component.text("Das Grundstück ", MessageManager.ERROR)
						.append(Component.text(protectionName, MessageManager.VARIABLE_VALUE)
								.append(Component.text(" konnte nicht gefunden werden.", MessageManager.ERROR))));
				return;
			}

			Builder header = Component.text();
			header.append(Component.text("<", MessageManager.SECONDARY));
			header.append(Component.text("--------------", MessageManager.SPACER));
			header.append(Component.text(" Grundstücksverwaltung ", MessageManager.PRIMARY));
			header.append(Component.text("--------------", MessageManager.SPACER));
			header.append(Component.text(">", MessageManager.SECONDARY));
			header.append(Component.newline());

			Location teleportLocation = regionInfo.getRegion().getFlag(Flags.TELE_LOC);
			Builder positionBuilder = null;

			if (teleportLocation != null) {
				positionBuilder = Component.text();
				positionBuilder.append(Component.text(teleportLocation.getBlockX(), MessageManager.VARIABLE_VALUE));
				positionBuilder.append(Component.text(", ", MessageManager.SPACER));
				positionBuilder.append(Component.text(teleportLocation.getBlockY(), MessageManager.VARIABLE_VALUE));
				positionBuilder.append(Component.text(", ", MessageManager.SPACER));
				positionBuilder.append(Component.text(teleportLocation.getBlockZ(), MessageManager.VARIABLE_VALUE));
			}

			Builder size = Component.text();
			size.append(Component.text(regionInfo.getArea(), MessageManager.VARIABLE_VALUE));
			size.append(Component.text(" Blöcke ", MessageManager.VARIABLE_VALUE));
			size.append(Component.text("[Vergrößern]", MessageManager.SUCCESS)
					.clickEvent(ClickEvent.runCommand("/protect expand " + regionInfo.getName()))
					.hoverEvent(HoverEvent.showText(
							Component.text("Klicke hier um das Grundstück zu vergrößern", MessageManager.SPACER))));

			Builder members = Component.text();
			List<LocalPlayer> membersList = regionInfo.getMembers();
			Iterator<LocalPlayer> memberIterator = membersList.iterator();

			members.append(Component.text("[", MessageManager.VARIABLE_VALUE));

			ProfileCache cache = WorldGuard.getInstance().getProfileCache();
			while (memberIterator.hasNext()) {
				LocalPlayer memberUser = memberIterator.next();
				String userName = memberUser.getName();
				if (userName == null) {
					Profile profile = cache.getIfPresent(memberUser.getUniqueId());

					if (profile != null) {
						userName = profile.getName();
					}
				}

				members.append(Component.text(userName, MessageManager.VARIABLE_VALUE)
						.clickEvent(ClickEvent
								.suggestCommand("/protect removeMember " + regionInfo.getName() + " "
										+ (userName != null ? userName : "Unknown")))
						.hoverEvent(HoverEvent.showText(
								Component.text("Klicke, um den Spieler zu entfernen", MessageManager.SPACER))));

				if (memberIterator.hasNext()) {
					members.append(Component.text(", ", MessageManager.SPACER));
				}

			}

			members.append(Component.text("]", MessageManager.VARIABLE_VALUE));

			members.append(Component.text(" [Hinzufügen]", MessageManager.SUCCESS)
					.clickEvent(ClickEvent.suggestCommand("/protect addMember " + regionInfo.getName() + " "))
					.hoverEvent(HoverEvent.showText(
							Component.text("Füge einen Spieler zu deinem Grundstück hinzu", MessageManager.SPACER))));
			members.append(Component.text(" [Entfernen]", MessageManager.ERROR)
					.clickEvent(ClickEvent.suggestCommand("/protect removeMember " + regionInfo.getName() + " "))
					.hoverEvent(HoverEvent.showText(Component.text("Entferne einen Spieler von deinem Grundstück hinzu",
							MessageManager.SPACER))));

			protectionUser.sendMessage(header.build());

			protectionUser.sendMessage(Component.text(" Name: ", MessageManager.VARIABLE_KEY)
					.append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_VALUE)));

			if (positionBuilder != null) {
				protectionUser.sendMessage(
						Component.text(" Position: ", MessageManager.VARIABLE_KEY).append(positionBuilder));
			}

			protectionUser
					.sendMessage(Component.text(" Größe: ", MessageManager.VARIABLE_KEY).append(size.build()));
			protectionUser
					.sendMessage(Component.text(" Mitglieder: ", MessageManager.VARIABLE_KEY).append(members.build()));
			protectionUser.sendMessage(Component.space());

		});
	}

}
