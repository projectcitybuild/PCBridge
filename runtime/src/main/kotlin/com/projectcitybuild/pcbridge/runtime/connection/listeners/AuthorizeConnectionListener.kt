package com.projectcitybuild.pcbridge.runtime.connection.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.runtime.connection.ConnectionRepository
import com.projectcitybuild.pcbridge.runtime.connection.connectionTracer
import com.projectcitybuild.pcbridge.runtime.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.runtime.listeners.runListenerSafelySync
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import kotlinx.coroutines.CancellationException
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
    fun handle(event: AsyncPlayerPreLoginEvent) = try {
        event.runListenerSafelySync(
            errorTracker,
            "An error occurred while authorizing a connection",
            onFailure = { disallowLogin(event) },
        ) {
            event.tracedSync(connectionTracer, this::class.java) {
                /*
                 * Block until authorization finishes. Paper reads the event result
                 * after handlers return, so a coroutine continuation would be too late.
                 * This Paper event is asynchronous, so we can block here safely.
                 * See https://github.com/Shynixn/MCCoroutine/issues/43
                 */
                runBlocking {
                    val authorization = connectionRepository.auth(
                        uuid = event.uniqueId,
                        ip = event.address,
                    )
                    val result = middlewareChain.pipe(
                        uuid = event.uniqueId,
                        ip = event.address,
                        authorization = authorization,
                    )
                    when (result) {
                        is ConnectionResult.Denied -> event.disallow(
                            AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                            result.reason,
                        )
                        is ConnectionResult.Allowed -> eventBroadcaster.broadcast(
                            ConnectionPermittedEvent(
                                playerData = authorization.player,
                                playerUUID = event.uniqueId,
                            ),
                        )
                    }
                }
            }
        }
    } catch (cancellation: CancellationException) {
        disallowLogin(event)
        throw cancellation
    }

    private fun disallowLogin(event: AsyncPlayerPreLoginEvent) {
        event.disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
            l10n.connection.errorUnableToAuthConnection,
        )
    }
}
