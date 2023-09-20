package com.projectcitybuild.modules.mutes.middleware

import com.projectcitybuild.pcbridge.core.architecture.events.EventMiddleware
import com.projectcitybuild.pcbridge.core.architecture.events.MiddlewareResult
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.support.textcomponent.send
import kotlinx.coroutines.withContext
import org.bukkit.event.player.AsyncPlayerChatEvent
import kotlin.coroutines.CoroutineContext

class MuteChatMiddleware(
    private val minecraftDispatcher: () -> CoroutineContext,
    private val playerConfigRepository: PlayerConfigRepository,
): EventMiddleware<AsyncPlayerChatEvent> {

    override suspend fun process(event: AsyncPlayerChatEvent): MiddlewareResult {
        val senderConfig = playerConfigRepository.get(event.player.uniqueId)!!

        if (senderConfig.isMuted) {
            withContext(minecraftDispatcher()) {
                println("Cancelling AsyncPlayerChatEvent")
                event.player.send().error("You cannot talk while muted")
                event.isCancelled = true
                print(event)
            }
            return MiddlewareResult.reject
        }
        return MiddlewareResult.allow
    }
}
