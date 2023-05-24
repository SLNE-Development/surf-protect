package dev.slne.protect.bukkit.region.settings;

/**
 * Represents the protection transaction cause
 */
public enum ProtectionTransactionCause {
	/**
	 * The protection was created
	 */
	NEW_PROTECTION,

	/**
	 * The protection was expanded
	 */
	EXPAND_PROTECTION,

	/**
	 * The protection was renamed
	 */
	RENAMED_PROTECTION,

	/**
	 * The protection was sold
	 */
	SOLD_PROTECTION;
}
