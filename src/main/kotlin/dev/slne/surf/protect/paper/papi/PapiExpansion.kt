package dev.slne.surf.protect.paper.papi

import dev.slne.surf.protect.paper.papi.placeholder.CurrentRegionPlaceholder
import dev.slne.surf.surfapi.bukkit.api.hook.papi.expansion.PapiExpansion

object PapiExpansion : PapiExpansion("surf-protect", listOf(CurrentRegionPlaceholder), "red")