package dev.slne.surf.protect.paper.region.settings

/**
 * Represents the protection transaction cause
 */
enum class ProtectionTransactionCause {
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
    SOLD_PROTECTION
}
