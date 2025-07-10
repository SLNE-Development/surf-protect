package dev.slne.surf.protect.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object ProtectPermissionRegistry : PermissionRegistry() {
    val PROTECTION_LIST_PERMISSION = create("surf.protect.list")
    val PROTECTION_VISUALIZE_PERMISSION = create("surf.protect.visualize")
    val PROTECTION_CREATE_PERMISSION = create("surf.protect.create")
    val PROTECTION_PLOT_MESSAGES = create("surf.protect.plotmessages")
    val PROTECTION_SETTINGS_PERMISSION = create("surf.protect.settings")
    val PROTECTION_TELEPORT = create("surf.protect.view.teleport")
    val PROTECTION_EXPAND = create("surf.protect.view.expand")
    val PROTECTION_RENAME = create("surf.protect.view.rename")
    val PROTECTION_EDIT_FLAGS = create("surf.protect.flags.edit")
    val PROTECTION_SELL = create("surf.protect.view.sell")
    val PROTECTION_MEMBER = create("surf.protect.view.members")

    private const val COMMAND_PREFIX = "surf.protect.command"
    val PROTECTION_WHO_COMMAND = create("$COMMAND_PREFIX.pwho")
    val PROTECTION_COMMAND = create("$COMMAND_PREFIX.protect")
    val MIGRATE_FLAGS_COMMAND = create("$COMMAND_PREFIX.migrateflags")
}