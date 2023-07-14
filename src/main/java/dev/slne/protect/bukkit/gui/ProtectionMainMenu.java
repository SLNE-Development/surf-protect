package dev.slne.protect.bukkit.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionType;
import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.gui.list.ProtectionListGui;
import dev.slne.protect.bukkit.gui.utils.ConfirmationGui;
import dev.slne.protect.bukkit.gui.utils.GuiUtils;
import dev.slne.protect.bukkit.gui.utils.ItemUtils;
import dev.slne.protect.bukkit.message.MessageManager;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.flags.ProtectionFlags;
import dev.slne.protect.bukkit.region.visual.visualizer.ProtectionVisualizer;
import dev.slne.protect.bukkit.user.ProtectionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtectionMainMenu extends ProtectionGui {

    private final Player targetProtectionPlayer;

    /**
     * Creates a new protection gui
     *
     * @param targetProtectionPlayer the player get the regions from
     */
    public ProtectionMainMenu(Player viewingPlayer, Player targetProtectionPlayer) {
        super(null, 5, "Protections - Menü", viewingPlayer);

        this.targetProtectionPlayer = targetProtectionPlayer;

        StaticPane navigationPane = new StaticPane(0, 0, 9, 5);

        if (viewingPlayer.hasPermission("surf.protect.list")) {
            navigationPane.addItem(getProtectionListItem(), 1, 2);
        }

        if (viewingPlayer.hasPermission("surf.protect.visualize")) {
            navigationPane.addItem(getVisualizerItem(), 2, 2);
        }

        if (viewingPlayer.hasPermission("surf.protect.create")) {
            navigationPane.addItem(getProtectionCreateItem(), 7, 2);
        }

        addPane(GuiUtils.getOutline(0));
        addPane(GuiUtils.getOutline(4));
        addPane(navigationPane);
    }

    /**
     * Gets the protection list item
     *
     * @return the protection list item
     */
    private GuiItem getProtectionListItem() {
        return new GuiItem(ItemUtils.item(Material.DIRT, 1, 0, Component.text("Liste", NamedTextColor.GOLD), Component.empty(), Component.text("Eine Liste von Protections", NamedTextColor.GRAY), Component.empty()), event -> {
            Player viewingPlayer = (Player) event.getWhoClicked();
            ProtectionUser user = ProtectionUser.getProtectionUser(getTargetProtectionPlayer());
            Map<World, List<ProtectedRegion>> regions = ProtectionUtils.getRegionListFor(user.getLocalPlayer());

            ProtectionListGui listGui = new ProtectionListGui(this, regions, viewingPlayer);
            listGui.show(viewingPlayer);
        });
    }

    /**
     * Gets the visualizer item
     *
     * @return the visualizer item
     */
    private GuiItem getVisualizerItem() {
        return new GuiItem(ItemUtils.item(Material.ENDER_EYE, 1, 0, Component.text("Visualizier", NamedTextColor.GOLD), Component.empty(), Component.text("Aktiviert/Deaktiviert den Visualizer", NamedTextColor.GRAY), Component.empty()), event -> {
            Player player = (Player) event.getWhoClicked();
            List<ProtectedRegion> regions =
                    ProtectionUtils.getRegionManager(player.getWorld()).getRegions().values().stream().filter(region -> !region.getType().equals(RegionType.GLOBAL)).toList();

            boolean state = BukkitMain.getBukkitInstance().getProtectionVisualizerState().getPlayerState(player);

            if (!state) {
                for (ProtectedRegion region : regions) {
                    State visualizeState = region.getFlag(ProtectionFlags.SURF_PROTECT_VISUALIZE);

                    if (visualizeState != null && visualizeState.equals(State.DENY)) {
                        continue;
                    }

                    BukkitMain.getBukkitInstance().getProtectionVisualizerThread().addVisualizer(player.getWorld(), region, player);
                }
            } else {
                for (ProtectionVisualizer<?> visualizer : new ArrayList<>(BukkitMain.getBukkitInstance().getProtectionVisualizerThread().getVisualizers(player))) {
                    visualizer.remove();
                }

                BukkitMain.getBukkitInstance().getProtectionVisualizerThread().removeVisualizers(player);
            }

            player.sendMessage(MessageManager.getProtectionVisualizeComponent(!state));

            Duration fadeDuration = Duration.ofMillis(150);
            Duration stayDuration = Duration.ofSeconds(1);

            player.showTitle(Title.title(Component.text("Visualizer", NamedTextColor.GOLD), Component.text("Visualizer " + (state ? "deaktiviert" : "aktiviert"), NamedTextColor.GRAY), Title.Times.times(fadeDuration, stayDuration, fadeDuration)));
            BukkitMain.getBukkitInstance().getProtectionVisualizerState().togglePlayerState(player);

            player.closeInventory();
        });
    }

    /**
     * Returns the protection create item
     *
     * @return the protection create item
     */
    private GuiItem getProtectionCreateItem() {
        return new GuiItem(ItemUtils.item(Material.GRASS_BLOCK, 1, 0, Component.text("Grundstück erstellen", NamedTextColor.GOLD), Component.empty(), Component.text("Erstelle ein neues Grundstück", NamedTextColor.GRAY), Component.empty()), event -> {
            Player player = (Player) event.getWhoClicked();

            List<Component> confirmLore = new ArrayList<>();
            confirmLore.addAll(ItemUtils.splitComponent("Bist du dir sicher, dass du ein Grundstück erstellen " + "m" + "öchtest?", 50, NamedTextColor.GRAY));

            ConfirmationGui confirmationGui = new ConfirmationGui(this, confirmEvent -> {
                ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
                ProtectionRegion regionCreation = new ProtectionRegion(protectionUser, null);
                protectionUser.startRegionCreation(regionCreation);

                MessageManager.sendProtectionModeEnterMessages(protectionUser);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        confirmEvent.getWhoClicked().closeInventory();
                    }
                }.runTaskLater(BukkitMain.getInstance(), 1);
            }, cancelEvent -> {

            }, Component.text("Grundstück erstellen", NamedTextColor.GOLD), confirmLore);

            confirmationGui.show(player);
        });
    }

    /**
     * @return the targetProtectionPlayer
     */
    public Player getTargetProtectionPlayer() {
        return targetProtectionPlayer;
    }
}
