package dev.slne.protect.paper.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.paper.command.commands.ProtectionWhoCommand;
import dev.slne.protect.paper.command.commands.protection.MigrateFlagCommand;
import dev.slne.protect.paper.command.commands.protection.ProtectionCommand;

import java.util.HashSet;
import java.util.Set;

public class BukkitCommandManager {

    private final Set<String> commands = new HashSet<>();

    /**
     * Register all commands
     */
    public void registerCommands() {
        register(new ProtectionCommand());
        register(new ProtectionWhoCommand());
        register(new MigrateFlagCommand());
    }

    public void unregisterCommands() {
        commands.forEach(CommandAPI::unregister);
    }

    private void register(CommandAPICommand command) {
        command.register();
        commands.add(command.getName());
    }

}
