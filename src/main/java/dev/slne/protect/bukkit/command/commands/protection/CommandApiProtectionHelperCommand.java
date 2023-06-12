package dev.slne.protect.bukkit.command.commands.protection;

import java.util.Arrays;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class CommandApiProtectionHelperCommand extends CommandAPICommand {

	/**
	 * Creates a new CommandApiProtectionHelperCommand
	 *
	 * @param commandName The name of the command
	 */
	public CommandApiProtectionHelperCommand(String commandName) {
		super(commandName);
	}

	/**
	 * Returns a header component
	 *
	 * @param headerContent The content of the header
	 * @return The header component
	 */
	public Component getHeaderComponent(String headerContent) {
		return this.getHeaderComponent(headerContent, 25);
	}

	/**
	 * Returns a header component
	 *
	 * @param headerContent   The content of the header
	 * @param arrowBaseLength The base length of the arrow
	 * @return The header component
	 */
	public Component getHeaderComponent(String headerContent, int arrowBaseLength) {
		TextComponent.Builder headerComponentBuilder = Component.text();
		int arrowModifier = 2;
		int arrowLength = arrowBaseLength - (headerContent.length() / arrowModifier) - 2 - 2;
		arrowLength = arrowLength <= 0 ? 1 : arrowLength;

		TextComponent.Builder arrowComponentBuilder = Component.text();

		String[] arrowChars = new String[arrowLength];
		Arrays.fill(arrowChars, "-");

		Arrays.asList(arrowChars).forEach(arrowChar -> arrowComponentBuilder
				.append(Component.text(arrowChar, MessageManager.SPACER)));

		Component arrowComponent = arrowComponentBuilder.build();

		headerComponentBuilder.append(Component.text("<", MessageManager.SECONDARY));
		headerComponentBuilder.append(arrowComponent);
		headerComponentBuilder.append(Component.text(" " + headerContent + " ", MessageManager.PRIMARY));
		headerComponentBuilder.append(arrowComponent);
		headerComponentBuilder.append(Component.text(">", MessageManager.SECONDARY));
		headerComponentBuilder.append(Component.newline());

		return headerComponentBuilder.build();
	}

}
