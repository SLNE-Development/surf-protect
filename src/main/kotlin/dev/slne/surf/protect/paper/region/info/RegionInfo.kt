package dev.slne.surf.protect.paper.region.info

import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import dev.slne.surf.protect.paper.math.Mth
import dev.slne.surf.protect.paper.region.flags.ProtectionFlagsRegistry
import dev.slne.surf.protect.paper.util.fixedVolume
import dev.slne.surf.protect.paper.util.getRegionManagerOrNull
import dev.slne.surf.protect.paper.util.toLocalPlayer
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf


data class RegionInfo(val region: ProtectedRegion) {
    var protectionFlagInfo: ProtectionFlagInfo

    val volume by lazy { this.region.fixedVolume() }
    val price get() = Mth.getRegionPrice(this.region)
    val retailPrice get() = Mth.getRegionRetailPrice(this.region)
    val name get() = protectionFlagInfo.name
    val centerLocation get() = region.getFlag(Flags.TELE_LOC)
    val regionManager get() = region.getRegionManagerOrNull()
    val owners get() = region.owners.uniqueIds.mapTo(mutableObjectListOf()) { it.toLocalPlayer() }
    val members get() = region.members.uniqueIds.mapTo(mutableObjectListOf()) { it.toLocalPlayer() }

    init {
        val flagInfo = region.getFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG)
        if (flagInfo == null) {
            protectionFlagInfo = ProtectionFlagInfo(region.id)
            region.setFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG, flagInfo)
        } else {
            protectionFlagInfo = flagInfo
        }
    }

    fun setProtectionInfoToRegion(info: ProtectionFlagInfo): ProtectionFlagInfo {
        protectionFlagInfo = info
        region.setFlag(ProtectionFlagsRegistry.SURF_PROTECT_FLAG, protectionFlagInfo)
        return protectionFlagInfo
    }
}
