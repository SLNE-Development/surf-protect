package dev.slne.protect.paper.gui.utils;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import dev.slne.protect.paper.gui.SurfGui;
import dev.slne.protect.paper.message.MessageManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemUtils {

  /**
   * Prevents instantiation.
   */
  private ItemUtils() {
  }

  /**
   * Creates a skull item.
   *
   * @param ownerUuid The UUID of the owner of the skull.
   * @param lore      The lore of the skull.
   * @return The created skull item.
   */
  public static ItemStack head(UUID ownerUuid, Component... lore) {
    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);

    return head(owner, Component.text(owner.getName() != null ? owner.getName() : "null",
            MessageManager.PRIMARY),
        lore);
  }

  /**
   * Creates a skull item.
   *
   * @param owner       The owner of the skull.
   * @param displayName The display name of the skull.
   * @param lore        The lore of the skull.
   * @return The created skull item.
   */
  public static ItemStack head(OfflinePlayer owner, Component displayName, Component... lore) {
    ItemStack head = item(Material.PLAYER_HEAD, 1, 0,
        displayName.colorIfAbsent(MessageManager.PRIMARY),
        lore);
    ItemMeta meta = head.getItemMeta();

    if (meta instanceof SkullMeta skullMeta) {
      skullMeta.setOwningPlayer(owner);
    }

    head.setItemMeta(meta);

    return head;
  }

  /**
   * Creates an {@link ItemStack} with the given parameters.
   *
   * @param material    The material of the item.
   * @param amount      The amount of the item.
   * @param durability  The durability of the item.
   * @param displayName The display name of the item.
   * @param lore        The lore of the item.
   * @return The created item.
   */
  public static ItemStack item(Material material, int amount, int durability, Component displayName,
      Component... lore) {
    ItemStack item = new ItemStack(material, amount);
    ItemMeta meta = item.getItemMeta();

    if (meta instanceof Damageable damageable) {
      damageable.setDamage(durability);
    }

    if (displayName == null) {
      displayName = Component.empty();
    }

    meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));

    if (lore != null) {
      List<Component> loreList = Arrays.asList(lore);
      loreList.replaceAll(line -> line.decoration(TextDecoration.ITALIC, false));

      meta.lore(loreList);
    }

    item.setItemMeta(meta);
    return item;
  }

  /**
   * Creates a pane item.
   *
   * @return The created pane item.
   */
  public static ItemStack paneItem() {
    return item(Material.GRAY_STAINED_GLASS_PANE, 1, 0, Component.space());
  }

  /**
   * Creates a close item.
   *
   * @return The created close item.
   */
  public static ItemStack closeItem() {
    return item(Material.BARRIER, 1, 0, Component.text("Schließen", MessageManager.PRIMARY),
        Component.empty(), Component.text("Schließt das Menü", NamedTextColor.GRAY),
        Component.empty());
  }

  /**
   * Creates a back item.
   *
   * @return The created back item.
   */
  public static ItemStack backItem(SurfGui gui) {
    List<SurfGui> parents = gui.walkParents();
    List<Component> parentNames = new ArrayList<>();

    String formatterParent = "<< %s";
    String formatterCurrent = ">> %s";

    PlainTextComponentSerializer serializer = PlainTextComponentSerializer.plainText();

    ComponentHolder mainTitleHolder = (ComponentHolder) gui.getGui().getTitleHolder();
    String mainTitle = serializer.serialize(mainTitleHolder.getComponent());
    Component mainName =
        Component.text(String.format(formatterCurrent, mainTitle),
            MessageManager.VARIABLE_VALUE);
    parentNames.add(mainName);
    for (SurfGui parent : parents) {
      ComponentHolder parentTitleHolder = (ComponentHolder) parent.getGui().getTitleHolder();
      String parentTitle = serializer.serialize(parentTitleHolder.getComponent());
      Component parentName =
          Component.text(String.format(formatterParent, parentTitle), MessageManager.SPACER);

      parentNames.add(parentName);
    }

    List<Component> lore = new ArrayList<>();
    lore.add(Component.empty());

    if (parentNames.size() > 1) {
      lore.add(Component.text("Geht zurück zum vorherigen Menü.", NamedTextColor.GRAY));
    } else {
      lore.add(Component.text("Schließt das Menü", NamedTextColor.GRAY));
    }

    lore.add(Component.empty());
    lore.addAll(parentNames);
    lore.add(Component.empty());

    return item(Material.ARROW, 1, 0, Component.text("Zurück", MessageManager.PRIMARY),
        lore.toArray(Component[]::new));
  }

  /**
   * Splits the given component into multiple components with the given max length
   *
   * @param component the component
   * @param maxLength the max length
   * @return the components
   */
  public static List<Component> splitComponent(String component, int maxLength, TextColor color) {
    List<Component> components = new ArrayList<>();
    String[] words = component.split(" ");
    StringBuilder builder = new StringBuilder();

    for (String word : words) {
      if (builder.length() + word.length() > maxLength) {
        components.add(Component.text(builder.toString(), color));
        builder = new StringBuilder();
      }
      builder.append(word).append(" ");
    }

    components.add(Component.text(builder.toString(), color));
    return components;
  }

  /**
   * Returns the confirmation item
   *
   * @return the confirmation item
   */
  public static ItemStack confirmationConfirmItem() {
    return item(Material.LIME_CONCRETE, 1, 0,
        Component.text("Bestätigen", MessageManager.PRIMARY),
        Component.empty(), Component.text("Bestätigt die Aktion", NamedTextColor.GRAY),
        Component.empty());
  }

  /**
   * Returns the cancel item
   *
   * @return the cancel item
   */
  public static ItemStack confirmationCancelItem() {
    return item(Material.RED_CONCRETE, 1, 0, Component.text("Abbrechen", MessageManager.PRIMARY),
        Component.empty(), Component.text("Bricht die Aktion ab", NamedTextColor.GRAY),
        Component.empty());
  }

  /**
   * Returns the question item
   *
   * @param displayName the display name
   * @param lore        the lore
   * @return the question item
   */
  public static ItemStack confirmationQuestionItem(Component displayName, Component... lore) {
    return item(Material.ENCHANTED_BOOK, 1, 0, displayName, lore);
  }

  /**
   * Modifies the lore of the given item
   *
   * @param item the item
   * @param lore the lore
   * @return the modified item
   */
  public static ItemStack modifyLore(ItemStack item, Component... lore) {
    ItemMeta meta = item.getItemMeta();

    if (lore != null) {
      List<Component> loreList = Arrays.asList(lore);
      loreList.replaceAll(line -> line.decoration(TextDecoration.ITALIC, false));

      meta.lore(loreList);
    }

    item.setItemMeta(meta);

    return item;
  }

}
