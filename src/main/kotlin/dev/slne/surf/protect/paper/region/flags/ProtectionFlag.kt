package dev.slne.surf.protect.paper.region.flags

import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.FlagContext
import dev.slne.surf.protect.paper.region.info.ProtectionFlagInfo
import dev.slne.surf.surfapi.core.api.util.object2ObjectMapOf

class ProtectionFlag(name: String) : Flag<ProtectionFlagInfo>(name) {
    override fun parseInput(context: FlagContext): ProtectionFlagInfo {
        val userInput = context.userInput
        return ProtectionFlagInfo(userInput)
    }

    override fun unmarshal(obj: Any?): ProtectionFlagInfo? {
        val map = obj as? MutableMap<String, Any> ?: return null
        val name = map["name"] as String
        return ProtectionFlagInfo(name)
    }

    override fun marshal(protectionFlagInfo: ProtectionFlagInfo) = object2ObjectMapOf<String, Any>(
        "name" to protectionFlagInfo.name
    )
}
