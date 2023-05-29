package dev.slne.protect.bukkit.command.commands;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;

public class ProtectionVisualizeCommand extends CommandAPICommand {

    private boolean state = true;

    public ProtectionVisualizeCommand() {
        super("pvisualize");

        executesPlayer((player, args) -> {
            List<ProtectedRegion> regions = ProtectionUtils.getRegionManager(player.getWorld()).getRegions().values()
                    .stream().filter(region -> !region.getType().equals(RegionType.GLOBAL)).toList();

            if (state) {
                for (ProtectedRegion region : regions) {
                    BukkitMain.getBukkitInstance().getProtectionVisualizerThread().addVisualizer(player.getWorld(),
                            region, player);
                }
            } else {
                for (ProtectionVisualizer<?> visualizer : new ArrayList<>(
                        BukkitMain.getBukkitInstance().getProtectionVisualizerThread().getVisualizers(player))) {
                    visualizer.remove();
                }

                BukkitMain.getBukkitInstance().getProtectionVisualizerThread().removeVisualizers(player);
            }

            player.sendMessage(MessageManager.getProtectionVisualizeComponent(state));
            state = !state;
        });

        register();
    }

}
