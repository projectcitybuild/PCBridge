package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.bans.usecases.AuthoriseConnectionUseCase
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerLoginEvent
import java.time.format.FormatStyle
import javax.inject.Inject

class BanConnectionListener @Inject constructor(
    private val authoriseConnectionUseCase: AuthoriseConnectionUseCase,
    private val logger: PlatformLogger,
    private val dateTimeFormatter: DateTimeFormatter,
    private val errorReporter: ErrorReporter,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onLoginEvent(event: PlayerLoginEvent) {
        runCatching {
            val ban = authoriseConnectionUseCase.getBan(
                uuid = event.player.uniqueId,
                ip = event.player.address,
            ) ?: return@runCatching

            val message = when (ban) {
                is AuthoriseConnectionUseCase.Ban.UUID -> TextComponent()
                    .add("You are currently banned.\n\n") {
                        it.color = ChatColor.RED
                        it.isBold = true
                    }
                    .add("Reason: ") { it.color = ChatColor.GRAY }
                    .add(ban.value.reason ?: "No reason provided") { it.color = ChatColor.WHITE }
                    .add("\n")
                    .add("Expires: ") { it.color = ChatColor.GRAY }
                    .add((ban.value.expiresAt?.let { dateTimeFormatter.convert(it, FormatStyle.SHORT) } ?: "Never") + "\n\n") { it.color = ChatColor.WHITE }
                    .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }

                is AuthoriseConnectionUseCase.Ban.IP -> TextComponent()
                    .add("You are currently IP banned.\n\n") {
                        it.color = ChatColor.RED
                        it.isBold = true
                    }
                    .add("Reason: ") { it.color = ChatColor.GRAY }
                    .add(ban.value.reason.ifEmpty { "No reason provided" }) { it.color = ChatColor.WHITE }
                    .add("\n")
                    .add("\n\n")
                    .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }
            }
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message.toLegacyText())
        }
            .onFailure { throwable ->
                throwable.message?.let { logger.fatal(it) }
                throwable.printStackTrace()

                errorReporter.report(throwable)

                // If something goes wrong, better not to let players in
                event.disallow(
                    PlayerLoginEvent.Result.KICK_OTHER,
                    "An error occurred while contacting the PCB authentication server. Please try again later"
                )
            }
    }
}
