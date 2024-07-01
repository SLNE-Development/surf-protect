package dev.slne.protect.bukkitold.listener.listeners;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import dev.slne.protect.bukkitold.BukkitMain;
import dev.slne.protect.bukkitold.gui.utils.ItemUtils;
import dev.slne.protect.bukkitold.region.ProtectionRegion;
import dev.slne.protect.bukkitold.region.ProtectionUtils;
import dev.slne.protect.bukkitold.region.settings.ProtectionSettings;
import dev.slne.protect.bukkitold.region.visual.Marker;
import dev.slne.protect.bukkitold.user.ProtectionUser;
import dev.slne.surf.surfapi.core.api.messages.Colors;
import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class ProtectionHotbarListener implements Listener, Colors {

  /**
   * The marker {@link ItemStack}
   */
  public static final ItemStack markerItem = ItemUtils.item(
      Material.REDSTONE_TORCH,
      ProtectionSettings.MARKERS,
      0,
      Component.text("Marker", YELLOW),
      Component.empty(),
      Component.text("Platziere die Marker um dein Grundstück zu definieren", GRAY),
      Component.empty()
  );

  /**
   * The {@link ItemStack} to accept the protection
   */
  public static final ItemStack acceptItem = ItemUtils.item(
      Material.LIME_CONCRETE,
      1,
      0,
      Component.text("Kaufen", GREEN),
      Component.empty(),
      Component.text("Kaufe das Grundstück", GRAY),
      Component.empty()
  );

  /**
   * The {@link ItemStack} to cancel the protection
   */
  public static final ItemStack cancelItem = ItemUtils.item(
      Material.RED_CONCRETE,
      1,
      0,
      Component.text("Abbrechen", RED),
      Component.empty(),
      Component.text("Bricht den Kauf ab", GRAY),
      Component.empty()
  );

  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (getProtectionUser(player).hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    final Player player = event.getPlayer();
    final ProtectionUser protectionUser = getProtectionUser(player);
    final ProtectionRegion regionCreation = protectionUser.getRegionCreation();

    if (!protectionUser.hasRegionCreation()) {
      return;
    }

    final ItemStack clickedItem = event.getItem();

    if (clickedItem == null || !clickedItem.hasItemMeta()) {
      return;
    }

    final ItemMeta itemMeta = clickedItem.getItemMeta();
    if (itemMeta == null || !itemMeta.hasDisplayName()) {
      return;
    }

    final Component displayName = itemMeta.displayName();

    assert displayName != null;

    if (displayName.equals(acceptItem.getItemMeta()
        .displayName())) { // TODO: 04.02.2024 09:54 - Use pdc instead of display name
      regionCreation.finishProtection();
      event.setCancelled(true);
    } else if (displayName.equals(cancelItem.getItemMeta().displayName())) {
      regionCreation.cancelProtection();
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    Player player = event.getPlayer();
    ProtectionUser protectionUser = getProtectionUser(player);

    if (block.getState().hasMetadata(ProtectionSettings.MARKER_KEY)) {
      Object markerObject = block.getState().getMetadata(ProtectionSettings.MARKER_KEY).get(0)
          .value();

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
    event.blockList()
        .removeIf(block -> block.getState().hasMetadata(ProtectionSettings.MARKER_KEY));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerBlockExplode(BlockExplodeEvent event) {
    event.blockList()
        .removeIf(block -> block.getState().hasMetadata(ProtectionSettings.MARKER_KEY));
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerPlace(BlockPlaceEvent event) {
    Block block = event.getBlock();
    Location location = block.getLocation();
    Player player = event.getPlayer();
    ProtectionUser protectionUser = getProtectionUser(player);

    if (ProtectionUtils.isInProtectionRegion(location)) {
      ItemStack clickedItem = event.getItemInHand();

      final ItemMeta itemMeta = clickedItem.getItemMeta();
      if (itemMeta == null || !itemMeta.hasDisplayName()) {
        return;
      }

      final Component displayName = itemMeta.displayName();
      assert displayName != null;

      if (displayName.equals(markerItem.getItemMeta().displayName())) {
        ProtectionRegion regionCreation = protectionUser.getRegionCreation();

        if (regionCreation == null) {
          event.setCancelled(true);
          return;
        }

        BlockData previousData = event.getBlockReplacedState().getBlockData();
        final Marker marker = regionCreation.createMarker(event.getBlock(), previousData, false);

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

  private ProtectionUser getProtectionUser(Player player) {
    return ProtectionUser.getProtectionUser(player);
  }
}
