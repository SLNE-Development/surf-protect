package dev.slne.protect.bukkit.command.commands.protection;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.slne.protect.bukkit.gui.ProtectionMainMenu;
import org.bukkit.OfflinePlayer;

public class ProtectionCommand extends CommandAPICommand {

    /**
     * Creates a new protection command
     */
    public ProtectionCommand() {
        super("protect");

        withPermission("surf.protect");

        withOptionalArguments(new OfflinePlayerArgument("player").withPermission("surf.protect.others"));

        executesPlayer((player, args) -> {
            final OfflinePlayer target = args.<OfflinePlayer>getOptionalUnchecked("player").orElse(player);
            final ProtectionMainMenu gui = new ProtectionMainMenu(player, target);

            gui.show(player);
        });
    }

}
