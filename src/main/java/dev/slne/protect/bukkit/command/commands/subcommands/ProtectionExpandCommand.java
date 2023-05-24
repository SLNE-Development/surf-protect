package dev.slne.protect.bukkit.command.commands.subcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ProtectionExpandCommand extends CommandAPICommand {

	public ProtectionExpandCommand() {
		super("expand");

		withPermission("survival.protect.expand");

		withSubcommand(new CommandAPICommand("confirm").withArguments(new StringArgument("protectionName2"))
				.executesPlayer((player, args) -> {
					ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
					RegionInfo regionInfo = getRegionInfo(protectionUser, (String) args.get("protectionName2"));

					if (regionInfo == null) {
						return;
					}

					ProtectionRegion regionCreation = new ProtectionRegion(protectionUser, regionInfo.getRegion());

					ProtectedRegion protectedRegion = regionInfo.getRegion();
					State canSellState = protectedRegion.getFlag(ProtectionFlags.SURVIVAL_CAN_SELL_FLAG);
					boolean canSell = canSellState == State.ALLOW || canSellState == null;

					if (!canSell) {
						protectionUser.sendMessage(MessageManager.prefix()
								.append(Component.text("Das Grundstück darf nicht erweitert werden.",
										MessageManager.ERROR)));
						return;
					}

					if (ProtectionUtils.standsInProtectedRegion(protectionUser.getBukkitPlayer(),
							regionInfo.getRegion())) {
						protectionUser.startRegionCreation(regionCreation);
						regionCreation.setExpandingMarkers();
					} else {
						protectionUser.sendMessage(MessageManager.prefix().append(Component.text(
								"Du befindest dich nicht auf dem zu erweiternden Grundstück.", MessageManager.ERROR)));
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
			RegionInfo regionInfo = getRegionInfo(protectionUser, (String) args.get("protectionName"));

			if (regionInfo == null)
				return;

			Builder infoText = Component.text();

			infoText.append(Component.text("Willkommen beim ProtectionSystem!", MessageManager.INFO));
			infoText.append(Component.newline());
			infoText.append(Component.text(
					"Wenn du den ProtectionMode betrittst, erhältst du vorübergehend Fly um dein Grundstück besser definieren zu können.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.text("Du definierst dein Grundstück indem du bis zu ", MessageManager.SPACER));
			infoText.append(Component.text(ProtectionSettings.MARKERS, MessageManager.SPACER));
			infoText.append(Component.text(" Marker platzierst und anschließend mit dem grünen Block bestätigst.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.text(
					"Mit dem roten Block kannst du die Protection jederzeit abbrechen und zu deinem Ausgangspunkt zurückkehren.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.newline());
			infoText.append(Component.text("ProtectionMode betreten (Klick)", MessageManager.SUCCESS)
					.clickEvent(ClickEvent
							.runCommand("/protect expand " + (String) args.get("protectionName2") + " confirm "
									+ (String) args.get("protectionName2")))
					.hoverEvent(HoverEvent.showText(
							Component.text("Klicke hier um den ProtectionMode zu betreten.",
									MessageManager.SPACER))));

			player.sendMessage(infoText.build());
		});
	}

	private RegionInfo getRegionInfo(ProtectionUser protectionUser, String regionName) {
		RegionInfo regionInfo = ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
				.filter(region -> {
					RegionInfo regionInfoItem = new RegionInfo(region.getValue());
					return regionInfoItem != null && regionInfoItem.getName().equals(regionName);
				}).map(region -> new RegionInfo(region.getValue())).findFirst().orElse(null);

		if (regionInfo == null) {
			protectionUser.sendMessage(Component.text("Das Grundstück existiert nicht.", MessageManager.ERROR));
			return null;
		}

		return regionInfo;
	}

}
