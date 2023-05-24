package dev.slne.protect.bukkit.region.info;

/**
 * A ProtectionFlagInfo is used on the actual WorldGuard region to store
 * attributes like the name and other values
 */
public class ProtectionFlagInfo {

	private final String name;

	/**
	 * Construct a new protection info
	 *
	 * @param name The name of the protection
	 */
	public ProtectionFlagInfo(String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the protection
	 *
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a copy of this protection info with a new name
	 *
	 * @param name The new name
	 * @return The copy
	 */
	public ProtectionFlagInfo copyWithNewName(String name) {
		return new ProtectionFlagInfo(name);
	}
}
