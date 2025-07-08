package dev.slne.protect.paper

import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import java.util.UUID
import java.util.concurrent.CompletableFuture

object KotlinConversationUtils {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getUuidAsync(username: String): CompletableFuture<UUID?> {
        return scope.future {
            PlayerLookupService.getUuid(username)
        }
    }
}