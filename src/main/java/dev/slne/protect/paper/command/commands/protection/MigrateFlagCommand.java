package dev.slne.protect.paper.command.commands.protection;

import com.sk89q.worldguard.protection.flags.StateFlag;
import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.paper.region.ProtectionUtils;
import dev.slne.protect.paper.region.flags.ProtectionFlagsRegistry;
import dev.slne.protect.paper.region.info.RegionInfo;

public class MigrateFlagCommand extends CommandAPICommand {

    /**
     * Creates a new protection command
     */
    public MigrateFlagCommand() {
        super("migrateflags");

        withPermission("surf.protect.admin");

        executesPlayer((player, args) -> {
            player.sendMessage("Updating flags...");
            ProtectionUtils.getRegionManager(player.getWorld()).getRegions().forEach((s, protectedRegion) -> {
                if (protectedRegion.getId().contains("-")) {
                    protectedRegion.setFlag(ProtectionFlagsRegistry.SURF_PROTECTION, StateFlag.State.ALLOW);
                    new RegionInfo(player.getWorld(), protectedRegion);
                }
            });
            player.sendMessage("Completed!");
        });
    }

}
