package dev.slne.protect.bukkit.command.commands.protection.rename;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.slne.protect.bukkit.command.commands.protection.ProtectionHelperCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
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
            for (Map.Entry<World, List<Map.Entry<String, ProtectedRegion>>> entry : ProtectionUtils
                    .getRegionsFor(protectionUser.getLocalPlayer()).entrySet()) {
                for (Map.Entry<String, ProtectedRegion> regionEntry : entry.getValue()) {
                    suggestions.add(regionEntry.getKey());
                }
            }
        }

        return suggestions;
    }

}
