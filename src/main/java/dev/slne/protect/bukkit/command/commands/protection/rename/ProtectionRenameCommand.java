package dev.slne.protect.bukkit.command.commands.protection.rename;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.slne.protect.bukkit.command.commands.protection.ProtectionHelperCommand;
import dev.slne.protect.bukkit.message.MessageManager;

public class ProtectionRenameCommand implements CommandExecutor, TabCompleter {

	private List<ProtectionHelperCommand> subCommands;

	/**
	 * The {@link ProtectionRenameCommand}
	 */
	public ProtectionRenameCommand(PluginCommand command) {
		command.setExecutor(this);

		subCommands = new ArrayList<>();
		subCommands.add(new ProtectionRenameConfirmCommand());
		subCommands.add(new ProtectionRenamePrintCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		Player player = (Player) sender;

		if (!sender.hasPermission("surf.protect.rename")) {
			sender.sendMessage(MessageManager.getNoPermissionComponent());
			return true;
		}

		for (ProtectionHelperCommand subCommand : subCommands) {
			if (subCommand.onCommand(player, args)) {
				return true;
			}
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String label, String[] args) {
		List<String> suggestions = new ArrayList<>();

		if (!(sender instanceof Player)) {
			return suggestions;
		}

		Player player = (Player) sender;

		if (!sender.hasPermission("surf.protect.rename")) {
			return suggestions;
		}

		for (ProtectionHelperCommand subCommand : subCommands) {
			List<String> tabCompletions = subCommand.tabComplete(player, args);
			if (tabCompletions != null) {
				suggestions.addAll(tabCompletions);
			}
		}

		return suggestions;
	}

}
