package dev.slne.protect.bukkit.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;

import dev.slne.protect.bukkit.BukkitMain;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.ProtectionUtils;
import dev.slne.protect.bukkit.region.settings.ProtectionSettings;
import dev.slne.protect.bukkit.region.visual.Marker;
import dev.slne.protect.bukkit.user.ProtectionUser;
import dev.slne.surf.gui.api.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ProtectionHotbarListener implements Listener {

    /**
     * The marker {@link ItemStack}
     */
    public static final ItemStack markerItem = ItemUtils.item(Material.REDSTONE_TORCH, ProtectionSettings.MARKERS, 0,
            Component.text("Marker", NamedTextColor.YELLOW), Component.empty(),
            Component.text("Platziere die Marker um dein Grundstück" + " zu " + "definieren",
                    NamedTextColor.GRAY),
            Component.empty());

    /**
     * The {@link ItemStack} to accept the protection
     */
    public static final ItemStack acceptItem = ItemUtils.item(Material.LIME_CONCRETE, 1, 0,
            Component.text("Kaufen", NamedTextColor.GREEN),
            Component.empty(), Component.text("Kaufe das Grundstück", NamedTextColor.GRAY), Component.empty());

    /**
     * The {@link ItemStack} to cancel the protection
     */
    public static final ItemStack cancelItem = ItemUtils.item(Material.RED_CONCRETE, 1, 0,
            Component.text("Abbrechen", NamedTextColor.RED),
            Component.empty(), Component.text("Bricht den Kauf ab", NamedTextColor.GRAY), Component.empty());

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);
        ProtectionRegion regionCreation = protectionUser.getRegionCreation();

        if (regionCreation != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        ProtectionRegion regionCreation = protectionUser.getRegionCreation();

        if (regionCreation == null) {
            return;
        }

        ItemStack clickedItem = event.getItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (clickedItem.getItemMeta().displayName().equals(acceptItem.getItemMeta().displayName())) {
            regionCreation.finishProtection();
            event.setCancelled(true);
        } else if (clickedItem.getItemMeta().displayName().equals(cancelItem.getItemMeta().displayName())) {
            regionCreation.cancelProtection();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMarkerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
            Object markerObject = block.getState().getMetadata(ProtectionSettings.MARKER_KEY).get(0).value();

            if (markerObject instanceof Marker marker) {
                ProtectionRegion creation = marker.getRegionCreation();
                if (protectionUser.getRegionCreation() != creation) {
                    event.setCancelled(true);
                    return;
                }

                creation.removeMarker(marker);
                block.getState().removeMetadata(ProtectionSettings.MARKER_KEY, BukkitMain.getInstance());

                event.setDropItems(false);
                this.setMarkerAmount(protectionUser);
            }

            return;
        }

        if (protectionUser.getRegionCreation() != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Set the amount of marker
     *
     * @param protectionUser the {@link ProtectionUser}
     */
    public void setMarkerAmount(ProtectionUser protectionUser) {
        ProtectionRegion regionCreation = protectionUser.getRegionCreation();
        ItemStack newMarkerItem = markerItem.clone();
        newMarkerItem.setAmount(regionCreation.getMarkerCountLeft());

        protectionUser.getBukkitPlayer().getInventory().setItem(0, newMarkerItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockToFrom(BlockFromToEvent event) {
        Block block = event.getToBlock();

        if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMarkerDestroy(BlockDestroyEvent event) {
        if (event.getBlock().getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonBreakMarker(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMarkerEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> block.getState().hasMetadata(ProtectionSettings.MARKER_KEY));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMarkerBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> block.getState().hasMetadata(ProtectionSettings.MARKER_KEY));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMarkerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();
        ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

        if (ProtectionUtils.isInProtectionRegion(location)) {
            ItemStack clickedItem = event.getItemInHand();

            if (clickedItem.getItemMeta() == null || !clickedItem.getItemMeta().hasDisplayName()) {
                return;
            }

            if (clickedItem.getItemMeta().displayName().equals(markerItem.getItemMeta().displayName())) {
                ProtectionRegion regionCreation = protectionUser.getRegionCreation();

                if (regionCreation == null) {
                    event.setCancelled(true);
                    return;
                }

                BlockData previousData = event.getBlockReplacedState().getBlockData();
                final Marker marker = regionCreation.createMarker(event.getBlock(), previousData);

                if (marker == null) {
                    event.setCancelled(true);
                    return;
                }

                location.getBlock().getState().setMetadata(ProtectionSettings.MARKER_KEY,
                        new FixedMetadataValue(BukkitMain.getInstance(), marker));
                setMarkerAmount(protectionUser);

                return;
            }
        }

        if (protectionUser.getRegionCreation() != null) {
            event.setCancelled(true);
        }
    }

}
