package dev.slne.protect.paper.gui.utils.sound;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum GuiSound {

  CLICK(Sound.UI_BUTTON_CLICK),
  BACK(Sound.UI_BUTTON_CLICK),
  ERROR(Sound.ENTITY_VILLAGER_NO),
  SUCCESS(Sound.ENTITY_PLAYER_LEVELUP),
  DENY_ACTION(Sound.ENTITY_VILLAGER_NO),
  CONFIRM_ACTION(Sound.ENTITY_VILLAGER_YES);

  private final Sound sound;

  /**
   * Creates a new gui sound.
   *
   * @param sound The sound to play.
   */
  GuiSound(Sound sound) {
    this.sound = sound;
  }

  /**
   * Gets the sound to play.
   *
   * @return The sound to play.
   */
  public Sound getSound() {
    return sound;
  }

  /**
   * Plays the sound to the player.
   *
   * @param player The player to play the sound to.
   */
  public void playSound(Player player, float volume, float pitch) {
    net.kyori.adventure.sound.Sound kyoriSound = net.kyori.adventure.sound.Sound.sound(sound,
        net.kyori.adventure.sound.Sound.Source.MASTER, volume, pitch);

    player.playSound(kyoriSound, net.kyori.adventure.sound.Sound.Emitter.self());
  }

}
