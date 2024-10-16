package com.projectcitybuild.pcbridge.features.bans.listeners

import com.projectcitybuild.pcbridge.core.datetime.DateTimeFormatter
import com.projectcitybuild.pcbridge.core.errors.SentryReporter
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.features.bans.actions.AuthorizeConnection
import com.projectcitybuild.pcbridge.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.http.responses.PlayerData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.time.format.FormatStyle
import kotlin.coroutines.CoroutineContext

class AuthorizeConnectionListener(
    private val playerRepository: PlayerRepository,
    private val authorizeConnection: AuthorizeConnection,
    private val dateTimeFormatter: DateTimeFormatter,
    private val sentry: SentryReporter,
    private val server: Server,
    private val minecraftDispatcher: () -> CoroutineContext,
) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun handle(event: AsyncPlayerPreLoginEvent) {
        /**
         * Events cannot be mutated after a suspended function has paused execution,
         * because the event would have been fully processed by the time execution is
         * resumed in this listener.
         *
         * We need to block so that the event processing pauses until this listener has
         * finished execution. Blocking shouldn't be a problem here because the listener
         * is already being executed asynchronously.
         *
         * See https://github.com/Shynixn/MCCoroutine/issues/43
         */
        runBlocking {
            runCatching {
                val playerData =
                    playerRepository.get(
                        playerUUID = event.uniqueId,
                        ip = event.address,
                    ) ?: PlayerData()

                when (val result = authorizeConnection.execute(playerData)) {
                    is AuthorizeConnection.ConnectResult.Denied -> event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        result.ban.toMessage(dateTimeFormatter),
                    )
                    is AuthorizeConnection.ConnectResult.Allowed -> {
                        withContext(minecraftDispatcher()) {
                            server.pluginManager.callEvent(
                                ConnectionPermittedEvent(
                                    playerData = playerData,
                                    playerUUID = event.uniqueId,
                                ),
                            )
                        }
                    }
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
