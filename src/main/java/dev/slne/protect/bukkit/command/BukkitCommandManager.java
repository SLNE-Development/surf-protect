package dev.slne.protect.bukkit.command;

import dev.slne.protect.bukkit.command.commands.ProtectionCommand;
import dev.slne.protect.bukkit.command.commands.ProtectionWhoCommand;

public class BukkitCommandManager {

    /**
     * Register all commands
     */
    public void registerCommands() {
        new ProtectionCommand();
        new ProtectionWhoCommand();
    }

}
