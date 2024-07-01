package dev.slne.protect.bukkit.player.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * The type Saved inventory.
 */
public class SavedInventory {

  private final ItemStack[] armorContents;
  private final ItemStack[] inventoryContents;
  private final ItemStack[] extraContents;

  /**
   * Instantiates a new Saved inventory.
   *
   * @param armorContents     the armor contents
   * @param inventoryContents the inventory contents
   * @param extraContents     the extra contents
   */
  public SavedInventory(ItemStack[] armorContents, ItemStack[] inventoryContents,
      ItemStack[] extraContents) {
    this.armorContents = armorContents;
    this.inventoryContents = inventoryContents;
    this.extraContents = extraContents;
  }

  /**
   * Instantiates a new Saved inventory.
   *
   * @param inventory the inventory
   */
  public SavedInventory(PlayerInventory inventory) {
    this.armorContents = inventory.getArmorContents();
    this.inventoryContents = inventory.getContents();
    this.extraContents = inventory.getExtraContents();
  }

  /**
   * Get armor contents item stack [ ].
   *
   * @return the item stack [ ]
   */
  public ItemStack[] getArmorContents() {
    return armorContents;
  }

  /**
   * Get inventory contents item stack [ ].
   *
   * @return the item stack [ ]
   */
  public ItemStack[] getInventoryContents() {
    return inventoryContents;
  }

  /**
   * Get extra contents item stack [ ].
   *
   * @return the item stack [ ]
   */
  public ItemStack[] getExtraContents() {
    return extraContents;
  }
}
