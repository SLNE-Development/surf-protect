package dev.slne.protect.bukkit.listener.listeners;

import com.destroystokyo.paper.MaterialSetTag;
import dev.slne.protect.bukkit.PaperMain;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.user.ProtectionUser;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ProtectionModeListener implements Listener {

  private static final MaterialSetTag INTERACTABLE = new MaterialSetTag(
      key("interactable")).contains("BUTTON").contains("DOOR").contains("LEVER").contains("GATE")
      .contains("TRAPDOOR").contains("ANVIL").contains("TABLE").contains("FURNACE")
      .contains("SMOKER").contains("BREWING_STAND").contains("LECTERN").contains("STONECUTTER")
      .contains("GRINDSTONE").contains("HOPPER").contains("DROPPER").contains("DISPENSER")
      .contains("CHEST").contains("SHULKER_BOX").contains("BARREL").contains("BEACON")
      .contains("BED").contains("CAULDRON").contains("COMPARATOR").contains("DAYLIGHT_DETECTOR")
      .contains("SIGN").contains("PRESSURE_PLATE").contains("JUKEBOX").contains("NOTE_BLOCK")
      .contains("BELL").contains("CAMPFIRE").contains("POT").add(Material.LOOM)
      .add(Material.REPEATER).add(Material.OBSERVER).add(Material.CHISELED_BOOKSHELF).lock();

  @Contract("_ -> new")
  private static @NotNull NamespacedKey key(String name) {
    return new NamespacedKey(PaperMain.getInstance(), name);
  }

  /**
   * Checks if the would interact instead of placing a block
   *
   * @param material the material
   * @return true if the material would be interacted with
   */
  private boolean isInteractive(Block block) {

    if (block == null) {
      return false;
    }

//    Material material = block.getType();
//    String name = material.toString();
//    Pattern pattern = Pattern.compile(
//        ".*(BUTTON|DOOR|LEVER|GATE|TRAPDOOR|ANVIL|TABLE|FURNACE|SMOKER|BREWING_STAND|LECTERN|STONECUTTER|LOOM|GRINDSTONE|HOPPER|DROPPER|DISPENSER|CHEST|"
//            + "SHULKER_BOX|BARREL|BEACON|BED|CAULDRON|COMPARATOR|DAYLIGHT_DETECTOR|REPEATER|OBSERVER|SIGN|PRESSURE_PLATE|JUKEBOX|NOTE_BLOCK|BELL|CAMPFIRE|POT|BOOKSHELF).*",
//        Pattern.CASE_INSENSITIVE);
//
//    return pattern.matcher(name).matches();

    return INTERACTABLE.isTagged(block);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onQuit(PlayerQuitEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

    if (protectionUser.hasRegionCreation()) {
      protectionUser.getRegionCreation().cancelProtection();
      protectionUser.resetRegionCreation();
    }
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
    ProtectionRegion regionCreation = protectionUser.getRegionCreation();

    if (regionCreation != null) {
      Location protectionModeLocation = regionCreation.getStartLocation();
      Player player = event.getPlayer();
      WorldBorder worldBorder = player.getWorldBorder();

      Location to = event.getTo();
      if (worldBorder != null && !worldBorder.isInside(to)) {
        event.getPlayer().teleport(protectionModeLocation);
      }

    }
  }

  @EventHandler
  public void onItemDrop(PlayerDropItemEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPortal(PlayerPortalEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }

    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onItemPickup(PlayerAttemptPickupItemEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onHandSwap(PlayerSwapHandItemsEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityAttack(PrePlayerAttackEntityEvent event) {
    Player player = event.getPlayer();
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityTarget(EntityTargetEvent event) {
    if (!(event.getTarget() instanceof Player player)) {
      return;
    }

    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(player);

    if (protectionUser.hasRegionCreation()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
    ProtectionRegion regionCreation = protectionUser.getRegionCreation();
    Block block = event.getClickedBlock();

    boolean isInProtectionMode = regionCreation != null;
    boolean isPlacingRedstoneTorch = event.getAction() == Action.RIGHT_CLICK_BLOCK
        && event.getMaterial() == Material.REDSTONE_TORCH;
    boolean isBreakingRedstoneTorch =
        event.getAction() == Action.LEFT_CLICK_BLOCK && (block.getType() == Material.REDSTONE_TORCH
            || block.getType() == Material.REDSTONE_WALL_TORCH);
    boolean isInteracting = isInteractive(block);

    // Check if the player is in protection mode else we don't want to do anything
    if (!isInProtectionMode) {
      return;
    }

    // Check if the player is interacting with a block instead of placing a redstone torch and if so cancel the event
    if (isInteracting && !event.getPlayer().isSneaking()) {
      event.setCancelled(true);
      return;
    }

    // Check if the player is placing or breaking a redstone torch and if not cancel the event
    if (!isPlacingRedstoneTorch && !isBreakingRedstoneTorch) {
      event.setCancelled(true);
    }

  }

  @EventHandler
  public void onItemFrameChange(PlayerItemFrameChangeEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
    ProtectionRegion regionCreation = protectionUser.getRegionCreation();

    if (regionCreation != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onArmorstandManipulate(PlayerArmorStandManipulateEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
    ProtectionRegion regionCreation = protectionUser.getRegionCreation();

    if (regionCreation != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityRide(PlayerInteractEntityEvent event) {
    ProtectionUser protectionUser = ProtectionUser.getProtectionUser(event.getPlayer());
    ProtectionRegion regionCreation = protectionUser.getRegionCreation();

    if (regionCreation != null) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onServerStop(
      PluginDisableEvent event) { // TODO: 04.02.2024 11:10 - move to a better place
    for (ProtectionUser protectionUser : PaperMain.getBukkitInstance().getUserManager()
        .getUsers()) {
      if (protectionUser.hasRegionCreation()) {
        protectionUser.getRegionCreation().cancelProtection();
      }
    }
  }
}
