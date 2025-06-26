package dev.slne.protect.bukkit

import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.CompletableFuture

object KotlinConversationUtils {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getUuidAsync(username: String): CompletableFuture<UUID?> {
        val future = CompletableFuture<UUID?>()
        scope.launch {
            try {
                val result = PlayerLookupService.getUuid(username)
                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
        return future
    }
}