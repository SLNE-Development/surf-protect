package dev.slne.protect.bukkit.command.commands;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;

public class ProtectionVisualizeCommand extends CommandAPICommand {

    private boolean state = true;

    public ProtectionVisualizeCommand() {
        super("pvisualize");

        executesPlayer((player, args) -> {
            ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
            List<ProtectedRegion> regions = ProtectionUtils.getRegionListFor(protectionUser.getLocalPlayer());

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

            player.sendMessage(
                    MessageManager.prefix()
                            .append(Component.text(state ? "Showing" : "Hiding",
                                    state ? MessageManager.SUCCESS : MessageManager.ERROR))
                            .append(Component.text(" visualizations.", MessageManager.INFO)));

            state = !state;
        });

        register();
    }

}
