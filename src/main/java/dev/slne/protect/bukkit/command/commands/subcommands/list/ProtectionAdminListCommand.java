package dev.slne.protect.bukkit.command.commands.subcommands.list;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionAdminListCommand extends ProtectionListCommand {

	public ProtectionAdminListCommand() {
		super("list");

		withPermission("survival.protect.list.admin");

		withArguments(new StringArgument("player")
				.replaceSuggestions(ArgumentSuggestions.strings(info -> Bukkit.getOnlinePlayers().stream()
						.map(Player::getName).toArray(size -> new String[size]))));
	}

	@Override
	public void provideList(ProtectionUser customSurvivalUser, ProtectionUser runAs, CommandArguments args) {

		if (!customSurvivalUser.getBukkitPlayer().hasPermission("survival.protect.list.admin")) {
			customSurvivalUser.sendMessage(MessageManager.prefix()
					.append(Component.text("Du hast keine Berechtigung für diesen Befehl!", MessageManager.ERROR)));
			return;
		}

		List<Map.Entry<String, ProtectedRegion>> playerRegions = ProtectionUtils.getRegionsFor(runAs.getLocalPlayer());

		customSurvivalUser.sendMessage(getHeaderComponent(runAs.getLocalPlayer().getName() + "'s Grundstücke"));

		playerRegions.stream().sorted((o1, o2) -> {
			ProtectedRegion region1 = o1.getValue();
			ProtectedRegion region2 = o2.getValue();

			RegionInfo regionInfo1 = new RegionInfo(region1);
			RegionInfo regionInfo2 = new RegionInfo(region2);

			return regionInfo1.getName().compareTo(regionInfo2.getName());
		}).forEach(entry -> {
			ProtectedRegion region = entry.getValue();
			RegionInfo regionInfo = new RegionInfo(region);

			Builder builder = Component.text();
			builder.append(Component.text(" - ", NamedTextColor.DARK_GRAY));
			builder.append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_KEY));
			builder.append(Component.text(": ", MessageManager.SPACER));
			builder.append(Component.text(regionInfo.getArea(), MessageManager.VARIABLE_VALUE));
			builder.append(Component.text(" Blöcke", MessageManager.VARIABLE_VALUE));

			Location teleportLocation = regionInfo.getRegion().getFlag(Flags.TELE_LOC);
			TextComponent.Builder positionBuilder = Component.text();

			if (teleportLocation != null) {
				positionBuilder.append(Component.text(teleportLocation.getBlockX(), MessageManager.VARIABLE_VALUE));
				positionBuilder.append(Component.text(", ", MessageManager.SPACER));
				positionBuilder.append(Component.text(teleportLocation.getBlockY(), MessageManager.VARIABLE_VALUE));
				positionBuilder.append(Component.text(", ", MessageManager.SPACER));
				positionBuilder.append(Component.text(teleportLocation.getBlockZ(), MessageManager.VARIABLE_VALUE));

				if (customSurvivalUser.getBukkitPlayer().getLocation().getWorld() == regionInfo.getTeleportLocation()
						.getWorld()) {
					builder.append(Component.text(" [", MessageManager.SPACER));
					builder.append(positionBuilder.build()
							.clickEvent(ClickEvent.runCommand(
									"/tp " + teleportLocation.getBlockX() + " " + teleportLocation.getBlockY() + " "
											+ teleportLocation.getBlockZ()))
							.hoverEvent(HoverEvent.showText(Component
									.text("Klicken, um dich zum Grundstück zu teleportieren", MessageManager.SPACER))));
					builder.append(Component.text("]", MessageManager.SPACER));

					builder.append(Component.text(" [Info]", MessageManager.SPACER)
							.clickEvent(ClickEvent.runCommand("/rg info " + regionInfo.getName()))
							.hoverEvent(HoverEvent.showText(
									Component.text("Klicken, um dir die Infos zu holen", MessageManager.SPACER))));
				}
			} else {
				positionBuilder
						.append(Component.text("Die Teleport-Location ist nicht gesetzt.", MessageManager.ERROR));
			}

			customSurvivalUser.sendMessage(builder.build());
		});

		customSurvivalUser.sendMessage(Component.space());
	}
}
