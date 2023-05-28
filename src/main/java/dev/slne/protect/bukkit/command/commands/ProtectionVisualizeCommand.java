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

            ProtectionVisualizer visualizer = BukkitMain.getBukkitInstance().getProtectionVisualizerManager()
                    .getVisualizer(protectionUser);

            if (visualizer == null) {
                visualizer = new ProtectionVisualizer(protectionUser);
                BukkitMain.getBukkitInstance().getProtectionVisualizerManager().addVisualizer(protectionUser,
                        visualizer);
            }

            visualizer.stopVisualizing();

            if (state) {
                for (ProtectedRegion region : regions.stream().map(Entry::getValue).toList()) {
                    visualizer.visualizeRegion(region);
                }
                player.sendMessage("showing");
            } else {
                player.sendMessage("hiding");
            }

            state = !state;
        });

        register();
    }

}
