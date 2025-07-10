package dev.slne.surf.protect.paper.region.visual

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import io.papermc.paper.math.BlockPosition
import kotlin.time.Duration.Companion.hours

object MarkerCache {
    private val markers = Caffeine.newBuilder()
        .expireAfterAccess(3.hours)
        .build<BlockPosition, Marker>()

    fun get(pos: BlockPosition): Marker? = markers.getIfPresent(pos)
    fun put(marker: Marker) {
        markers.put(marker.pos, marker)
    }

    fun invalidate(marker: Marker) {
        markers.invalidate(marker.pos)
    }
}