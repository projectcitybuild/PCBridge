package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.aggregate.ConnectPlayerUseCase
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.support.spigot.logger.PlatformLogger
import com.projectcitybuild.support.textcomponent.add
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.time.format.FormatStyle
import javax.inject.Inject

class AsyncPreLoginListener @Inject constructor(
    private val connectPlayerUseCase: ConnectPlayerUseCase,
    private val logger: PlatformLogger,
    private val dateTimeFormatter: DateTimeFormatter,
    private val errorReporter: ErrorReporter,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPreLogin(event: AsyncPlayerPreLoginEvent) {

        // Events cannot be mutated (i.e. we cannot kick the connecting player) inside a
        // suspended function due to the way the code compiles, so we need to block.
        // Blocking isn't a problem here because the event observation is already running asynchronously
        //
        // See https://github.com/Shynixn/MCCoroutine/issues/43
        runBlocking {
            runCatching {
                val result = connectPlayerUseCase.execute(
                    playerUUID = event.uniqueId,
                    ip = event.address.toString(),
                )
                if (result is ConnectPlayerUseCase.ConnectResult.Denied) {
                    event.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                        result.ban.toMessage(dateTimeFormatter),
                    )
                }
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

private fun ConnectPlayerUseCase.Ban.toMessage(
    dateTimeFormatter: DateTimeFormatter,
): String {
    return when (this) {
        is ConnectPlayerUseCase.Ban.UUID -> TextComponent()
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

        is ConnectPlayerUseCase.Ban.IP -> TextComponent()
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
