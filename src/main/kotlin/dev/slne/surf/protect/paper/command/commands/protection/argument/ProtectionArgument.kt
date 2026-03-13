package dev.slne.surf.protect.paper.command.commands.protection.argument

import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.toObjectList

class ProtectionArgument(nodeName: String) :
    CustomArgument<ProtectedRegion, String>(StringArgument(nodeName), { info ->
        WorldGuard.getInstance().platform.regionContainer.loaded.flatMap { it.regions.values }
            .filter { it.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) != null }
            .firstOrNull { it.id == info.input }
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Das Grundstück wurde nicht gefunden.")
                })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                WorldGuard.getInstance().platform.regionContainer.loaded.flatMap { it.regions.values }
                    .filter { it.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) != null }
                    .toObjectList().map { it.id }
            }
        )
    }
}

inline fun CommandTree.protectionArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    ProtectionArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.protectionArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    ProtectionArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.protectionArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(ProtectionArgument(nodeName).setOptional(optional).apply(block))