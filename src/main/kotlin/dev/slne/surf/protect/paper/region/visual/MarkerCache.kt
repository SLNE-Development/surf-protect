package dev.slne.surf.protect.paper.region.visual

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.shynixn.mccoroutine.folia.launch
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.protect.paper.plugin
import io.papermc.paper.math.BlockPosition
import kotlin.time.Duration.Companion.hours

object MarkerCache {
    private val markers = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .evictionListener<BlockPosition, Marker> { pos, marker, cause ->
            if (cause.wasEvicted() && marker != null && !marker.isRestored()) {
                plugin.launch { marker.restorePreviousData() }
            }
        }
        .build<BlockPosition, Marker>()

    fun get(pos: BlockPosition): Marker? = markers.getIfPresent(pos)
    fun put(marker: Marker) {
        markers.put(marker.pos, marker)
    }

    fun invalidate(marker: Marker) {
        markers.invalidate(marker.pos)
    }
}