package com.projectcitybuild.pcbridge.paper.runtime.connection.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.paper.runtime.connection.connectionTracer
import com.projectcitybuild.pcbridge.paper.runtime.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.runtime.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.runtime.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.paper.runtime.listeners.scopedSync
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AuthorizeConnectionListener(
    private val middlewareChain: ConnectionMiddlewareChain,
    private val connectionRepository: ConnectionRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val errorTracker: ErrorTracker,
) : Listener {
    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true,
    )
    fun handle(event: AsyncPlayerPreLoginEvent) =
        event.scopedSync(connectionTracer, this::class.java) {
            /**
             * To call `event.disallow()` the handler must block until player data
             * is fetched, because it mutates a flag on the Event instance that Paper
             * reads after all handlers run.
             *
             * MCCoroutine returns the event immediately and continues suspending
             * functions in the background, so mutating the event in a coroutine
             * would occur too late (i.e. after it's already read).
             *
             * See https://github.com/Shynixn/MCCoroutine/issues/43
             */
            runBlocking {
                runCatching {
                    val authorization =
                        connectionRepository.auth(
                            uuid = event.uniqueId,
                            ip = event.address,
                        )
                    val result =
                        middlewareChain.pipe(
                            uuid = event.uniqueId,
                            ip = event.address,
                            authorization = authorization,
                        )
                    when (result) {
                        is ConnectionResult.Denied ->
                            event.disallow(
                                AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                                result.reason,
                            )
                        is ConnectionResult.Allowed ->
                            eventBroadcaster.broadcast(
                                ConnectionPermittedEvent(
                                    playerData = authorization.player,
                                    playerUUID = event.uniqueId,
                                ),
                            )
                    }
                }.onFailure {
                    log.error(it) { "An error occurred while authorizing a connection" }
                    errorTracker.report(it)

                    eventBroadcaster.broadcast(
                        ConnectionPermittedEvent(
                            playerData = null,
                            playerUUID = event.uniqueId,
                        ),
                    )
                }
            }
        }
}
