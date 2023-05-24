package dev.slne.protect.bukkit.command.commands;

import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionAddMemberCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionCreateCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionExpandCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionInfoCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionRemoveMemberCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionSellCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.list.ProtectionAdminListCommand;
import dev.slne.protect.bukkit.command.commands.subcommands.list.ProtectionUserListCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

public class ProtectionCommand extends CommandAPICommand {

	public ProtectionCommand() {
		super("protect");

		withAliases("protection", "claim");
		withPermission("survival.protect");

		withSubcommand(new ProtectionUserListCommand());
		withSubcommand(new ProtectionAdminListCommand());
		withSubcommand(new ProtectionCreateCommand());
		withSubcommand(new ProtectionInfoCommand());
		withSubcommand(new ProtectionExpandCommand());
		withSubcommand(new ProtectionAddMemberCommand());
		withSubcommand(new ProtectionRemoveMemberCommand());
		withSubcommand(new ProtectionSellCommand());

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
			sendCommandDescription(player, "/protect create", "Erstelle ein neues Grundstück",
					"Betrete den ProtectionMode und erstelle ein neues Grundstück");
			sendCommandDescription(player, "/protect expand <Grundstück>", "Vergrößere ein vorhandenes Grundstück",
					"Betrete den ProtectionMode und erweitere ein bestehendes Grundstück. Du musst dich auf dem Grundstück befinden.");
			sendCommandDescription(player, "/protect list", "Zeige alle deine Grundstücke an",
					"Zeigt dir eine Liste aller deiner aktuellen Grundstücke an");
			sendCommandDescription(player, "/protect info <Grundstück>", "Öffne die Info Seite eines Grundstückes",
					"Öffnet eine Verwaltungsübersicht des entsprechenden Grundstücks. Hier kann du dein Grundstück auch vergrößern oder Freunde hinzufügen.");
			sendCommandDescription(player, "/protect addMember <Grundstück> <Spieler>",
					"Fügt ein Mitglied zum Grundstück hinzu",
					"Gibt dem angegebenem Spieler die Berechtigung auf dem Grundstück zu bauen.");
			sendCommandDescription(player, "/protect removeMember <Grundstück> <Spieler>",
					"Entfernt ein Mitglied vom Grundstück hinzu",
					"Entzieht dem angegebenem Spieler die Berechtigung auf dem Grundstück zu bauen.");
			sendCommandDescription(player, "/protect sell <Grundstück>", "Verkaufe das angegebene Grundstück",
					"Verkauft das angegebene Grundstück und erstattet einen Teilbetrag zurück");
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
