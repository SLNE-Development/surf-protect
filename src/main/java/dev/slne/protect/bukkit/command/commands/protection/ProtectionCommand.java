package dev.slne.protect.bukkit.command.commands.protection;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.slne.protect.bukkit.gui.ProtectionMainMenu;
import dev.slne.protect.bukkit.message.MessageManager;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ProtectionCommand extends CommandAPICommand {

    /**
     * Creates a new protection command
     */
    public ProtectionCommand() {
        super("protect");

        withPermission("surf.protect");

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

            ProtectionMainMenu gui = new ProtectionMainMenu(player, target);
            gui.show(player);
        });

        register();
    }

}
