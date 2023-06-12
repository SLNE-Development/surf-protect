package dev.slne.protect.bukkit.command.commands.protection.rename;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import dev.slne.protect.bukkit.command.commands.protection.ProtectionHelperCommand;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.info.ProtectionFlagInfo;
import dev.slne.protect.bukkit.region.info.RegionInfo;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionRenameConfirmCommand implements ProtectionHelperCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (args.length == 5) {
            String protectionName = args[0];
            String protectionDisplayName = args[1];
            String protectionNameConfirm = args[3];
            String protectionDisplayNameConfirm = args[4];

            if (!args[2].equalsIgnoreCase("confirm")) {
                return false;
            }

            if (!protectionName.equals(protectionNameConfirm)
                    || !protectionDisplayName.equals(protectionDisplayNameConfirm)) {
                return true;
            }

            // Check if user has enough currency
            ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
            RegionInfo regionInfo = ProtectionUtils.getRegionInfo(protectionUser.getLocalPlayer(), protectionName);

            return protectionUser.hasEnoughCurrency(ProtectionSettings.PROTECTION_RENAME_PRICE)
                    .thenApplyAsync(hasEnoughCurrency -> {
                        if (Boolean.TRUE.equals(hasEnoughCurrency)) {
                            if (ProtectionUtils.standsInProtectedRegion(protectionUser.getBukkitPlayer(),
                                    regionInfo.getRegion())) {
                                protectionUser.addTransaction(-ProtectionSettings.PROTECTION_RENAME_PRICE);
                                ProtectionFlagInfo protectionInfo = new ProtectionFlagInfo(protectionDisplayName);

                                regionInfo.getRegion().setFlag(ProtectionFlags.SURF_PROTECT_FLAG,
                                        protectionInfo);

                                player.sendMessage(MessageManager.getProtectionRenamedComponent());
                                return true;
                            } else {
                                protectionUser
                                        .sendMessage(MessageManager.getNotStandingOnRenameProtectionComponent());
                                return true;
                            }
                        } else {
                            player.sendMessage(MessageManager.getTooExpensiveToRenameComponent());
                            return true;
                        }
                    }).join();
        }

        return false;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return new ArrayList<>();
    }

}
