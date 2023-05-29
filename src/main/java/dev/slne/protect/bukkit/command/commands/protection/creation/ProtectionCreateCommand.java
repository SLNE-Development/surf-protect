package dev.slne.protect.bukkit.command.commands.protection.creation;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ProtectionCreateCommand extends CommandAPICommand {

	public ProtectionCreateCommand() {
		super("create");

		withPermission("surf.protect.create");

		withSubcommand(new CommandAPICommand("confirm").executesPlayer((player, args) -> {
			ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
			ProtectionRegion regionCreation = new ProtectionRegion(protectionUser, null);
			protectionUser.startRegionCreation(regionCreation);
		}));

		executesPlayer((player, args) -> {

			Builder infoText = Component.text();

			infoText.append(Component.text("Willkommen beim ProtectionSystem!", MessageManager.INFO));
			infoText.append(Component.newline());
			infoText.append(Component.text(
					"Wenn du den ProtectionMode betrittst, erhältst du vorübergehend Fly um dein Grundstück besser definieren zu können.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.text("Du definierst dein Grundstück indem du bis zu ",
					MessageManager.SPACER));
			infoText.append(Component.text(ProtectionSettings.MARKERS,
					MessageManager.SPACER));
			infoText.append(Component.text(" Marker platzierst und anschließend mit dem grünen Block bestätigst.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.text(
					"Mit dem roten Block kannst du die Protection jederzeit abbrechen und zu deinem Ausgangspunkt zurückkehren.",
					MessageManager.SPACER));
			infoText.append(Component.newline());
			infoText.append(Component.newline());
			infoText.append(Component.text("ProtectionMode betreten (Klick)", MessageManager.SUCCESS)
					.clickEvent(ClickEvent.runCommand("/protect create confirm")).hoverEvent(HoverEvent.showText(
							Component.text("Klicke hier um den ProtectionMode zu betreten.", MessageManager.SPACER))));

			player.sendMessage(infoText.build());

		});

	}

}
