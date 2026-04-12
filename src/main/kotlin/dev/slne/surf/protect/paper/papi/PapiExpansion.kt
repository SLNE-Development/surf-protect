package dev.slne.surf.protect.paper.papi

import dev.slne.surf.api.paper.hook.papi.expansion.PapiExpansion
import dev.slne.surf.protect.paper.papi.placeholder.CurrentRegionPlaceholder

object PapiExpansion : PapiExpansion("surf-protect", listOf(CurrentRegionPlaceholder), "red")