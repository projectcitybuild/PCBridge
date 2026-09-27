package com.projectcitybuild.pcbridge.paper.platform.paper.events

import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.bukkit.event.Event
import kotlin.coroutines.CoroutineContext

class SpigotEventBroadcaster(
    private val server: Server,
    private val minecraftDispatcher: () -> CoroutineContext,
) {
    suspend fun broadcast(event: Event) {
        withContext(minecraftDispatcher()) {
            server.pluginManager.callEvent(event)
        }
    }
}
