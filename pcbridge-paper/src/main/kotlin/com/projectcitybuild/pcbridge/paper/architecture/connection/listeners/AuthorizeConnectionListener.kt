package com.projectcitybuild.pcbridge.paper.architecture.connection.listeners

import com.projectcitybuild.pcbridge.paper.architecture.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddlewareChain
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.architecture.connection.repositories.PlayerRepository
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AuthorizeConnectionListener(
    private val middlewareChain: ConnectionMiddlewareChain,
    private val playerRepository: PlayerRepository,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val sentry: SentryReporter,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun handle(event: AsyncPlayerPreLoginEvent) {
        /**
         * In order to call `event.disallow()`, this function must block until player data
         * has been fetched and processed. Blocking is not a problem because this event
         * handler function is called asynchronously by Spigot/Paper.
         *
         * The `event.disallow()` function works by mutating a boolean on the Event class instance.
         * The final value is read from the instance after all event handlers have had their turn.
         * However, event handlers are not suspending as they are Java functions. MCCoroutine gets
         * around this by immediately returning the event but allowing suspending functions to
         * continue execution in the background.
         *
         * Therefore, in order to call a mutating function, the event handler must be blocking so that
         * it awaits the result of the handler.
         *
         * See https://github.com/Shynixn/MCCoroutine/issues/43
         */
        runBlocking {
            runCatching {
                val playerData = playerRepository.get(
                    uuid = event.uniqueId,
                    ip = event.address,
                )
                val result = middlewareChain.run(
                    uuid = event.uniqueId,
                    ip = event.address,
                    playerData = playerData,
                )
                when (result) {
                    is ConnectionResult.Allowed -> eventBroadcaster.broadcast(
                        ConnectionPermittedEvent(
                            playerData = playerData,
                            playerUUID = event.uniqueId,
                        ),
                    )
                    is ConnectionResult.Denied -> event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        result.reason,
                    )
                }
            }.onFailure {
                log.error(it) { "An error occurred while authorizing a connection" }
                sentry.report(it)

                // If something goes wrong, permit the connection without player data
                // so that their permissions are all stripped
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