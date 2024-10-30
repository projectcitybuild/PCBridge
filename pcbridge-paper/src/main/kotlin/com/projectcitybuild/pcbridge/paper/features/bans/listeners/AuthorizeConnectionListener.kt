package com.projectcitybuild.pcbridge.paper.features.bans.listeners

import com.projectcitybuild.pcbridge.paper.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.paper.core.logger.log
import com.projectcitybuild.pcbridge.paper.features.bans.actions.AuthorizeConnection
import com.projectcitybuild.pcbridge.paper.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.paper.features.bans.utilities.toMiniMessage
import com.projectcitybuild.pcbridge.paper.support.spigot.SpigotEventBroadcaster
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AuthorizeConnectionListener(
    private val playerRepository: PlayerRepository,
    private val authorizeConnection: AuthorizeConnection,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val sentry: SentryReporter,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun handle(event: AsyncPlayerPreLoginEvent) {
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
                    playerUUID = event.uniqueId,
                    ip = event.address,
                )
                when (val result = authorizeConnection.authorize(playerData)) {
                    is AuthorizeConnection.ConnectResult.Denied -> event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        result.ban.toMessage(),
                    )
                    is AuthorizeConnection.ConnectResult.Allowed -> eventBroadcaster.broadcast(
                        ConnectionPermittedEvent(
                            playerData = playerData,
                            playerUUID = event.uniqueId,
                        ),
                    )
                }
            }.onFailure {
                log.error(it) { "An error occurred while authorizing a connection" }
                sentry.report(it)

                // If something goes wrong, permit the connection without player data
                // so that their permissions are all stripped
                ConnectionPermittedEvent(
                    playerData = null,
                    playerUUID = event.uniqueId,
                )
            }
        }
    }
}

private fun AuthorizeConnection.Ban.toMessage() = when (this) {
    is AuthorizeConnection.Ban.UUID -> value.toMiniMessage()
    is AuthorizeConnection.Ban.IP -> value.toMiniMessage()
}
