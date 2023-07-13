package com.projectcitybuild.modules.moderation.bans.listeners

import com.projectcitybuild.features.aggregate.AuthoriseConnection
import com.projectcitybuild.features.aggregate.GetAggregate
import com.projectcitybuild.features.aggregate.SyncPlayerWithAggregate
import com.projectcitybuild.libs.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import com.projectcitybuild.support.textcomponent.add
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.time.format.FormatStyle

// TODO: split this up and move it into appropriate modules
class AsyncPreLoginListener(
    private val getAggregate: GetAggregate,
    private val authoriseConnection: AuthoriseConnection,
    private val syncPlayerWithAggregate: SyncPlayerWithAggregate,
    private val logger: PlatformLogger,
    private val dateTimeFormatter: DateTimeFormatter,
    private val errorReporter: ErrorReporter,
) : SpigotListener<AsyncPlayerPreLoginEvent> {

    @EventHandler(priority = EventPriority.HIGHEST)
    override suspend fun handle(event: AsyncPlayerPreLoginEvent) {
        /**
         * Events cannot be mutated (i.e. we cannot kick the connecting player) inside a
         * suspended function due to the way the code compiles, so we need to block.
         * Blocking isn't a problem here because the event observation is already running asynchronously
         *
         * See https://github.com/Shynixn/MCCoroutine/issues/43
         */
        runBlocking {
            runCatching {
                val aggregate = getAggregate.execute(
                    playerUUID = event.uniqueId,
                    ip = event.address.toString(),
                )

                val result = authoriseConnection.execute(aggregate)
                if (result is AuthoriseConnection.ConnectResult.Denied) {
                    event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        result.ban.toMessage(dateTimeFormatter),
                    )
                    return@runBlocking
                }

                syncPlayerWithAggregate.execute(
                    playerUUID = event.uniqueId,
                    aggregate = aggregate,
                )
            }.onFailure { throwable ->
                throwable.message?.let { logger.fatal(it) }
                throwable.printStackTrace()

                errorReporter.report(throwable)

                // If something goes wrong, better not to let players in
                event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    "An error occurred while contacting the PCB authentication server. Please try again later"
                )
            }
        }
    }
}

private fun AuthoriseConnection.Ban.toMessage(
    dateTimeFormatter: DateTimeFormatter,
): String {
    return when (this) {
        is AuthoriseConnection.Ban.UUID -> TextComponent()
            .add("You are currently banned.\n\n") {
                it.color = ChatColor.RED
                it.isBold = true
            }
            .add("Reason: ") { it.color = ChatColor.GRAY }
            .add(value.reason ?: "No reason provided") { it.color = ChatColor.WHITE }
            .add("\n")
            .add("Expires: ") { it.color = ChatColor.GRAY }
            .add((value.expiresAt?.let { dateTimeFormatter.convert(it, FormatStyle.SHORT) } ?: "Never") + "\n\n") { it.color = ChatColor.WHITE }
            .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }

        is AuthoriseConnection.Ban.IP -> TextComponent()
            .add("You are currently IP banned.\n\n") {
                it.color = ChatColor.RED
                it.isBold = true
            }
            .add("Reason: ") { it.color = ChatColor.GRAY }
            .add(value.reason.ifEmpty { "No reason provided" }) { it.color = ChatColor.WHITE }
            .add("\n")
            .add("\n\n")
            .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }
    }
        .toLegacyText()
}