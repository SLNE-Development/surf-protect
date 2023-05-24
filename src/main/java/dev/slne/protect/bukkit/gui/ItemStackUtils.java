package dev.slne.protect.bukkit.gui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;

/**
 * Utils for {@link ItemStack}s
 *
 */
public class ItemStackUtils {

	/**
	 * Utility class
	 */
	private ItemStackUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Gets a new {@link ItemStack} by the given values
	 *
	 * @param material    the {@link Material}
	 * @param amount      the {@link ItemStack} amount
	 * @param damage      the {@link ItemStack} damage
	 * @param displayName the {@link ItemStack} displayName
	 * @param lore        the {@link ItemStack} lore
	 * @return the created {@link ItemStack}
	 */
	public static ItemStack getItem(Material material, int amount, int damage, String displayName, String... lore) {
		return getItem(material, amount, damage, Component.text(displayName),
				Arrays.asList(lore).stream().map(Component::text).collect(Collectors.toList()));
	}

	/**
	 * Gets a new {@link ItemStack} by the given values
	 *
	 * @param material    the {@link Material}
	 * @param amount      the {@link ItemStack} amount
	 * @param damage      the {@link ItemStack} damage
	 * @param displayName the {@link ItemStack} displayName
	 * @param lore        the {@link ItemStack} lore
	 * @return the created {@link ItemStack}
	 */
	public static ItemStack getItem(Material material, int amount, int damage, Component displayName,
			Component... lore) {
		return getItem(material, amount, damage, displayName, Arrays.asList(lore));
	}

	/**
	 * Gets a new {@link ItemStack} by the given values
	 *
	 * @param material    the {@link Material}
	 * @param amount      the {@link ItemStack} amount
	 * @param damage      the {@link ItemStack} damage
	 * @param displayName the {@link ItemStack} displayName
	 * @param lore        the {@link ItemStack} lore
	 * @return the created {@link ItemStack}
	 */
	public static ItemStack getItem(Material material, int amount, int damage, Component displayName,
			List<Component> lore) {
		ItemStack itemStack = new ItemStack(material, amount);

		if (itemStack.getItemMeta() instanceof Damageable) {
			Damageable damageableItemMeta = (Damageable) itemStack.getItemMeta();
			damageableItemMeta.setDamage(damage);

			itemStack.setItemMeta(damageableItemMeta);
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.displayName(displayName);

		if (lore != null && !lore.isEmpty()) {
			itemMeta.lore(lore);
		}

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
}
