package dev.slne.surf.protect.paper.papi

import dev.slne.surf.api.paper.hook.papi.expansion.PapiExpansion
import dev.slne.surf.protect.paper.papi.placeholder.CurrentRegionPlaceholder
import dev.slne.surf.protect.paper.papi.placeholder.IsCurrentRegionSpawnPlaceholder

object PapiExpansion : PapiExpansion(
    "surf-protect", listOf(
        CurrentRegionPlaceholder,
        IsCurrentRegionSpawnPlaceholder
    ), "red"
)