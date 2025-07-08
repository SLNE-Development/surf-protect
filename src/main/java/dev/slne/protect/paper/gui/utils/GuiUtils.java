package dev.slne.protect.paper.gui.utils;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.slne.protect.paper.PaperMain;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.gui.utils.sound.GuiSound;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiUtils {

  /**
   * Prevents instantiation.
   */
  private GuiUtils() {
  }

  /**
   * Plays a gui sound to the player.
   *
   * @param sound  The gui sound to play.
   * @param player The player to play the gui sound to.
   */
  public static void playGuiSound(GuiSound sound, Player player) {
    playSound(sound.getSound(), player);
  }

  /**
   * Plays a sound to the player.
   *
   * @param toPlay The sound to play.
   * @param player The player to play the sound to.
   */
  public static void playSound(org.bukkit.Sound toPlay, Player player) {
    Sound sound = Sound.sound().type(toPlay).volume(.5f).pitch(1f)
        .source(Sound.Source.MASTER).build();
    Sound.Emitter emitter = Sound.Emitter.self();

    player.playSound(sound, emitter);
  }

  /**
   * Creates an outline pane with the given row.
   *
   * @param row The row of the outline pane.
   * @return The created outline pane.
   */
  public static OutlinePane getOutline(int row) {
    OutlinePane outline = new OutlinePane(0, row, 9, 1);

    outline.setPriority(Pane.Priority.LOWEST);
    outline.setRepeat(true);
    outline.addItem(new GuiItem(ItemUtils.paneItem(), event -> event.setCancelled(true)));

    return outline;
  }

  /**
   * Creates a navigation pane with the given row.
   *
   * @param gui The gui to create the navigation pane for.
   * @param row The row of the navigation pane.
   * @return The created navigation pane.
   */
  public static StaticPane getNavigation(SurfGui gui, int row) {
    StaticPane navigation = new StaticPane(0, row, 9, 1);
    navigation.setPriority(Pane.Priority.HIGHEST);

    int closeX = gui.hasParent() ? 5 : 4;

    if (gui.hasParent()) {
      navigation.addItem(new GuiItem(ItemUtils.backItem(gui), event -> {
        event.setCancelled(true);
        gui.backToParent(event.getWhoClicked());
      }), 3, 0);
    }

    navigation.addItem(new GuiItem(ItemUtils.closeItem(), event -> {
      event.setCancelled(true);

      new BukkitRunnable() {
        @Override
        public void run() {
          event.getWhoClicked().closeInventory();
        }
      }.runTaskLater(PaperMain.getInstance(), 1);
    }), closeX, 0);

    return navigation;
  }

}
