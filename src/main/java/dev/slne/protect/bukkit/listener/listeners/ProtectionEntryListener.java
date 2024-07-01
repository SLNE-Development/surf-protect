package dev.slne.protect.bukkit.listener.listeners;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.slne.protect.bukkit.listener.event.protection.ProtectionEnterEvent;
import dev.slne.protect.bukkit.listener.event.protection.ProtectionExitEvent;
import dev.slne.protect.bukkit.player.ProtectionPlayer;
import dev.slne.protect.bukkit.region.ProtectionRegion;
import dev.slne.protect.bukkit.region.flag.ProtectionFlagsRegistry;
import dev.slne.protect.bukkit.region.flag.info.ProtectionFlagInfo;
import dev.slne.surf.surfapi.core.api.messages.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * The type Protection entry listener.
 */
public class ProtectionEntryListener implements Listener {

  /**
   * On protection enter.
   *
   * @param event the event
   */
  @EventHandler
  public void onProtectionEnter(ProtectionEnterEvent event) {
    handleEntryOrExitMessage(event.getEnteredPlayer(), event.getEnteredProtectionRegion(), true);
  }

  /**
   * On protection exit.
   *
   * @param event the event
   */
  @EventHandler
  public void onProtectionExit(ProtectionExitEvent event) {
    handleEntryOrExitMessage(event.getExitedPlayer(), event.getExitedProtectionRegion(), false);
  }

  /**
   * Handle entry or exit message.
   *
   * @param player  the player
   * @param region  the region
   * @param entered the entered
   */
  private void handleEntryOrExitMessage(ProtectionPlayer player, ProtectionRegion region,
      boolean entered) {
    ProtectedRegion protectedRegion = region.getWorldGuardRegion();

    ProtectionFlagInfo protectionFlagInfo = protectedRegion.getFlag(
        ProtectionFlagsRegistry.SURF_PROTECT_INFO);
    StateFlag.State surfProtectionFlag = protectedRegion.getFlag(
        ProtectionFlagsRegistry.SURF_PROTECT_IS_PROTECTION);

    // Check if it is a player protection
    if (surfProtectionFlag == null || surfProtectionFlag == StateFlag.State.DENY
        || protectionFlagInfo == null) {
      return;
    }

    Player bukkitPlayer = player.getPlayer();
    PersistentDataContainer pdc = bukkitPlayer.getPersistentDataContainer();
    boolean sendMessage = pdc.getOrDefault(ProtectionFlagsRegistry.PLOT_MESSAGES_KEY,
        PersistentDataType.BOOLEAN, ProtectionFlagsRegistry.DEFAULT_PLOT_MESSAGES);

    if (!sendMessage) {
      return;
    }

    TextComponent.Builder builder = Component.text();
    builder.append(Colors.PREFIX);
    builder.append(Component.text("Du hast das Grundstück ", Colors.INFO));
    builder.append(Component.text(protectionFlagInfo.getName(), Colors.VARIABLE_VALUE));
    builder.append(Component.text(entered ? " betreten." : " verlassen.", Colors.INFO));

    bukkitPlayer.sendMessage(builder.build());
  }

}
