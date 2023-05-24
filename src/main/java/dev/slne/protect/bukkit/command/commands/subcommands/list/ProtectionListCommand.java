package dev.slne.protect.bukkit.command.commands.subcommands.list;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;

import dev.jorel.commandapi.executors.CommandArguments;
import dev.slne.protect.bukkit.command.commands.subcommands.ProtectionHelperCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.protect.bukkit.user.ProtectionUserFinder;
import net.kyori.adventure.text.Component;

public abstract class ProtectionListCommand extends ProtectionHelperCommand {

	protected ProtectionListCommand(String commandName) {
		super(commandName);

		executesPlayer((player, args) -> {
			this.provideList0(player, args);
		});
	}

	public abstract void provideList(ProtectionUser user, ProtectionUser runAs, CommandArguments args);

	private void provideList0(Player player, CommandArguments args) {
		ProtectionUser protectionUser = null;

		if (args.count() > 0) {
			String playerName = (String) args.get("player");

			LocalPlayer localPlayer = ProtectionUserFinder.findLocalPlayer(playerName);
			if (localPlayer == null) {
				player.sendMessage(MessageManager.prefix()
						.append(Component.text("Der Spieler ", MessageManager.ERROR)
								.append(Component.text((String) args.get("player"), MessageManager.VARIABLE_VALUE))
								.append(Component.text(" konnte nicht gefunden werden!", MessageManager.ERROR))));
				return;
			}

			protectionUser = ProtectionUser.getProtectionUser(player);

		}

		ProtectionUser user = ProtectionUser.getProtectionUser(player);
		this.provideList(user, protectionUser, args);
	}

}
