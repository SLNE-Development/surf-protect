package dev.slne.protect.bukkit.command.commands;

import java.util.List;
import java.util.Map.Entry;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;
import dev.slne.protect.bukkit.user.ProtectionUser;

public class ProtectionVisualizeCommand extends CommandAPICommand {

    private boolean state = true;

    public ProtectionVisualizeCommand() {
        super("pvisualize");

        executesPlayer((player, args) -> {
            ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

            List<Entry<String, ProtectedRegion>> regions = ProtectionUtils
                    .getRegionsFor(protectionUser.getLocalPlayer());

            for (ProtectedRegion protectedRegion : regions.stream().map(Entry::getValue).toList()) {
                ProtectionVisualizer visualizer = new ProtectionVisualizer(protectedRegion, protectionUser);

                if (state) {
                    BukkitMain.getBukkitInstance().getVisualizerTask().addVisualizer(visualizer);
                    visualizer.visualize();
                    player.sendMessage("showing");
                } else {
                    BukkitMain.getBukkitInstance().getVisualizerTask().removeVisualizers(player);
                    player.sendMessage("hiding");
                }
            }

            state = !state;
        });

        register();
    }

}
