package dev.slne.protect.bukkit.command;

import dev.slne.protect.bukkit.command.commands.ProtectionOldCommand;
import dev.slne.protect.bukkit.command.commands.ProtectionWhoCommand;
import dev.slne.protect.bukkit.command.commands.protection.ProtectionCommand;

public class BukkitCommandManager {

    /**
     * Register all commands
     */
    public void registerCommands() {
        new ProtectionCommand();
        new ProtectionOldCommand();
        new ProtectionWhoCommand();
    }

}
