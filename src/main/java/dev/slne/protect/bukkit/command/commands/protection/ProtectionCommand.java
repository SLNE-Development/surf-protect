package dev.slne.protect.bukkit.command.commands.protection;

import java.util.Optional;

import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.slne.protect.bukkit.gui.ProtectionGui;
import dev.slne.protect.bukkit.message.MessageManager;

public class ProtectionCommand extends CommandAPICommand {

    /**
     * Creates a new protection command
     */
    public ProtectionCommand() {
        super("protect");

        withOptionalArguments(new PlayerArgument("player"));

        executesPlayer((player, args) -> {
            Optional<Object> targetOptional = args.getOptional("player");
            Player target = null;

            if (targetOptional.isPresent()) {
                target = (Player) targetOptional.get();
            } else {
                target = player;
            }

            if (target == null) {
                player.sendMessage(MessageManager.getPlayerNotFoundComponent());
                return;
            }

            ProtectionGui gui = new ProtectionGui(player, target);
            gui.show(player);
        });

        register();
    }

}
