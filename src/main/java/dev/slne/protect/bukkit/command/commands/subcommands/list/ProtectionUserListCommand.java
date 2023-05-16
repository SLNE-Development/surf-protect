package dev.slne.protect.bukkit.command.commands.subcommands.list;

import java.util.Map;
import java.util.Set;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.regions.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.utils.ProtectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionUserListCommand extends ProtectionListCommand {

	public ProtectionUserListCommand() {
		super("list");

		withPermission("survival.protect.list");
	}

	@Override
	public void provideList(ProtectionUser protectionUser, ProtectionUser runAs, CommandArguments args) {

		Set<Map.Entry<String, ProtectedRegion>> playerRegions = ProtectionUtils
				.getRegionsFor(protectionUser.getLocalPlayer());

		Builder header = Component.text();
		header.append(Component.text("<", MessageManager.SECONDARY));
		header.append(Component.text("--------------", MessageManager.SPACER));
		header.append(Component.text(" Deine Grundstücke ", MessageManager.PRIMARY));
		header.append(Component.text("--------------", MessageManager.SPACER));
		header.append(Component.text(">", MessageManager.SECONDARY));
		header.append(Component.newline());

		protectionUser.sendMessage(header.build());

		playerRegions.stream().sorted((o1, o2) -> {
			ProtectedRegion region1 = o1.getValue();
			ProtectedRegion region2 = o2.getValue();

			RegionInfo regionInfo1 = new RegionInfo(region1);
			RegionInfo regionInfo2 = new RegionInfo(region2);

			return regionInfo1.getName().compareTo(regionInfo2.getName());
		}).forEach((entry) -> {
			ProtectedRegion region = entry.getValue();
			RegionInfo regionInfo = new RegionInfo(region);

			int distance = (int) (protectionUser.getBukkitPlayer().getLocation().getWorld()
					.equals(regionInfo.getTeleportLocation().getWorld())
							? protectionUser.getBukkitPlayer().getLocation()
									.distance(regionInfo.getTeleportLocation())
							: -1);

			Component clickComponent = Component.text()
					.append(Component.text(regionInfo.getName(), MessageManager.VARIABLE_KEY))
					.clickEvent(ClickEvent.runCommand("/protect info " + regionInfo.getName()))
					.hoverEvent(
							HoverEvent.showText(Component.text("Mehr Informationen... (Klick)", MessageManager.SPACER)))
					.build();

			Builder builder = Component.text();
			builder.append(Component.text(" - ", NamedTextColor.DARK_GRAY));
			builder.append(clickComponent);
			builder.append(Component.text(": ", MessageManager.SPACER));
			builder.append(Component.text(regionInfo.getArea(), MessageManager.VARIABLE_VALUE));
			builder.append(Component.text(" Blöcke ", MessageManager.VARIABLE_VALUE));

			if (protectionUser.getBukkitPlayer().getLocation().getWorld() == regionInfo.getTeleportLocation()
					.getWorld()) {
				builder.append(Component.text("(Entfernung: ", MessageManager.SPACER));
				builder.append(Component.text(distance, MessageManager.SPACER));
				builder.append(Component.text(" Blöcke)", MessageManager.SPACER));
			}

			protectionUser.sendMessage(builder.build());
		});

		protectionUser.sendMessage(Component.space());
	}
}
