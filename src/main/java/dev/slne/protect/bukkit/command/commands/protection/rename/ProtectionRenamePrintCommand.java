package dev.slne.protect.bukkit.command.commands.protection.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import dev.slne.protect.bukkit.command.commands.protection.ProtectionHelperCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionRenamePrintCommand implements ProtectionHelperCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (args.length == 2) {
            String protectionName = args[0];
            String protectionDisplayName = args[1];

            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("/prename ");
            commandBuilder.append(protectionName);
            commandBuilder.append(" ");
            commandBuilder.append(protectionDisplayName);
            commandBuilder.append(" confirm ");
            commandBuilder.append(protectionName);
            commandBuilder.append(" ");
            commandBuilder.append(protectionDisplayName);

            String command = commandBuilder.toString();
            player.sendMessage(MessageManager.getProtectionRenameComponent(command));

            return true;
        }

        return false;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        List<String> suggestions = new ArrayList<>();
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (args.length == 1) {
            suggestions.addAll(Arrays
                    .asList(ProtectionUtils.getRegionsFor(protectionUser.getLocalPlayer()).stream()
                            .map(region -> new RegionInfo(region.getValue()).getName())
                            .toArray(size -> new String[size]))
                    .stream()
                    .filter(info -> info.toLowerCase().contains(args[0].toLowerCase())).toList());
        }

        return suggestions;
    }

}
