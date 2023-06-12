package dev.slne.protect.bukkit.command.commands;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.command.commands.protection.member.ProtectionAddMemberCommand;
import dev.slne.protect.bukkit.command.commands.protection.member.ProtectionRemoveMemberCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ProtectionOldCommand extends CommandAPICommand {

	public ProtectionOldCommand() {
		super("protecto");

		withAliases("protection", "claim");
		withPermission("surf.protect");

		withSubcommand(new ProtectionAddMemberCommand());
		withSubcommand(new ProtectionRemoveMemberCommand());

		executesPlayer((player, args) -> {
			Builder header = Component.text();
			header.append(Component.text("<", MessageManager.SECONDARY));
			header.append(Component.text("--------------", MessageManager.SPACER));
			header.append(Component.text(" ProtectionSystem ", MessageManager.PRIMARY));
			header.append(Component.text("--------------", MessageManager.SPACER));
			header.append(Component.text(">", MessageManager.SECONDARY));
			header.append(Component.newline());

			player.sendMessage(header.build());

			sendCommandDescription(player, "/protect", "Zeige die Befehlsübersicht",
					"Öffnet die aktuell angezeigte Befehlsübersicht");
			sendCommandDescription(player, "/protect addMember <Grundstück> <Spieler>",
					"Fügt ein Mitglied zum Grundstück hinzu",
					"Gibt dem angegebenem Spieler die Berechtigung auf dem Grundstück zu bauen.");
			sendCommandDescription(player, "/protect removeMember <Grundstück> <Spieler>",
					"Entfernt ein Mitglied vom Grundstück hinzu",
					"Entzieht dem angegebenem Spieler die Berechtigung auf dem Grundstück zu bauen.");
			sendCommandDescription(player, "/pwho", "Überprüfe ob du auf einem Grundstück stehst",
					"Zeige das Grundstück und dessen Eigentümer an, auf welchem du dich gerade befindest.");

			player.sendMessage(Component.space());
		});

		register();
	}

	/**
	 * Sends the command description
	 *
	 * @param sender           the {@link CommandSender}
	 * @param command          the command
	 * @param shortDescription the short description
	 * @param longDescription  the long description
	 */
	public void sendCommandDescription(CommandSender sender, String command, String shortDescription,
			String longDescription) {
		Builder commandDescription = Component.text();
		commandDescription.append(Component.text(" - ", MessageManager.SPACER));
		commandDescription.append(Component.text(command, MessageManager.VARIABLE_VALUE)
				.clickEvent(ClickEvent.suggestCommand(command))
				.hoverEvent(HoverEvent.showText(Component.text(command, MessageManager.VARIABLE_VALUE)
						.append(Component.newline().append(Component.text(longDescription, MessageManager.SPACER))))));
		commandDescription.append(Component.space());
		commandDescription.append(Component.text(shortDescription, MessageManager.SPACER));
		sender.sendMessage(commandDescription.build());
	}

}
