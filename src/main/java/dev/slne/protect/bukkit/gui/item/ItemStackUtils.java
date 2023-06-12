package dev.slne.protect.bukkit.gui.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
		itemMeta.displayName(displayName != null ? displayName.decoration(TextDecoration.ITALIC, false) : null);

		if (lore != null && !lore.isEmpty()) {
			List<Component> loreWithDecoration = lore.stream().map(component -> component
					.decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList());

			itemMeta.lore(loreWithDecoration);
		}

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	/**
	 * Returns a close item stack
	 *
	 * @return the close item stack
	 */
	public static ItemStack getCloseItemStack() {
		return getItem(Material.BARRIER, 1, 0, Component.text("Schließen", NamedTextColor.RED),
				Component.text("Schließt das Inventar", NamedTextColor.GRAY));
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
}
