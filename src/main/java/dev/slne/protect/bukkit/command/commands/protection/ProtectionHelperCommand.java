package dev.slne.protect.bukkit.command.commands.protection;

import java.util.List;

import org.bukkit.entity.Player;

public interface ProtectionHelperCommand {

    /**
     * Returns the command execution
     *
     * @param player the {@link Player}
     * @param args   the arguments
     * @return the command execution
     */
    public boolean onCommand(Player player, String[] args);

    /**
     * Returns the tab completion
     *
     * @param player the {@link Player}
     * @param args   the arguments
     * @return the tab completion
     */
    public List<String> tabComplete(Player player, String[] args);

}
