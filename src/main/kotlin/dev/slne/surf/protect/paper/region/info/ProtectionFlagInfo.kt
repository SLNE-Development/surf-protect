package dev.slne.surf.protect.paper.region.info

/**
 * A ProtectionFlagInfo is used on the actual WorldGuard region to store
 * attributes like the name and other values
 *
 * @param name The name of the protection
 */
@JvmRecord
data class ProtectionFlagInfo(val name: String)
