package dev.slne.surf.protect.paper.command.commands.protection

import com.sk89q.worldguard.protection.flags.StateFlag
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.protect.paper.message.Messages
import dev.slne.surf.protect.paper.permission.ProtectPermissionRegistry
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.region.info.RegionInfo
import dev.slne.surf.protect.paper.util.regionContainer


fun migrateFlagCommand() = commandAPICommand("migrateflags") {
    withPermission(ProtectPermissionRegistry.MIGRATE_FLAGS_COMMAND)

    anyExecutor { sender, _ ->
        sender.sendMessage(Messages.Command.MigrateFlags.started)

        for (manager in regionContainer.loaded) {
            for ((_, region) in manager.regions) {
                if (region.id.contains("-")) {
                    region.setFlag(ProtectionFlagsRegistry.SURF_PROTECTION, StateFlag.State.ALLOW)
                    RegionInfo(region)
                }
            }
        }

        sender.sendMessage(Messages.Command.MigrateFlags.completed)
    }
}