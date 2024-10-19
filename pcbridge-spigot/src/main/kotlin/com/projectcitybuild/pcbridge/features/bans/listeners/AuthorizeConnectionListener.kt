package com.projectcitybuild.pcbridge.features.bans.listeners

import com.projectcitybuild.pcbridge.core.datetime.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.features.bans.actions.AuthorizeConnection
import com.projectcitybuild.pcbridge.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.support.spigot.SpigotEventBroadcaster
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.time.format.FormatStyle

class AuthorizeConnectionListener(
    private val playerRepository: PlayerRepository,
    private val authorizeConnection: AuthorizeConnection,
    private val dateTimeFormatter: DateTimeFormatter,
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
                        result.ban.toMessage(dateTimeFormatter),
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

                // If something goes wrong, better not to let players in
                event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text("An error occurred while contacting the PCB authentication server. Please try again later"),
                )
            }
        }
    }
}

private fun AuthorizeConnection.Ban.toMessage(dateTimeFormatter: DateTimeFormatter): Component {
    return when (this) {
        is AuthorizeConnection.Ban.UUID ->
            MiniMessage.miniMessage().deserialize(
                """
                <color:red><b>You are currently banned.</b></color>
                
                <color:gray>Reason:</color> ${value.reason ?: "No reason provided"}
                <color:gray>Expires:</color> ${value.expiresAt?.let { dateTimeFormatter.convert(it, FormatStyle.SHORT) } ?: "Never"}
                
                <color:aqua>Appeal @ https://projectcitybuild.com"</color>
                """.trimIndent(),
            )

        is AuthorizeConnection.Ban.IP ->
            MiniMessage.miniMessage().deserialize(
                """
                <color:red><b>You are currently IP banned.</b></color>
                
                <color:gray>Reason:</color> ${value.reason.ifEmpty { "No reason provided" }}
                
                <color:aqua>Appeal @ https://projectcitybuild.com</color>
                """.trimIndent(),
            )
    }
}
