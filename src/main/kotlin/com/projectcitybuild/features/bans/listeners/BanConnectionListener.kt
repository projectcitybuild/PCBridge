package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.features.bans.repositories.IPBanRepository
import com.projectcitybuild.modules.datetime.DateTimeFormatter
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.platforms.bungeecord.extensions.add
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import java.time.format.FormatStyle
import javax.inject.Inject

class BanConnectionListener @Inject constructor(
    private val plugin: Plugin,
    private val banRepository: BanRepository,
    private val ipBanRepository: IPBanRepository,
    private val logger: PlatformLogger,
    private val dateTimeFormatter: DateTimeFormatter,
    private val errorReporter: ErrorReporter,
) : BungeecordListener {

    // TODO: is there a way to inject this in the constructor separately from Dagger's auto-injection?
    var dispatcher: CoroutineDispatcher = Dispatchers.IO

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onLoginEvent(event: LoginEvent) {
        event.registerIntent(plugin)

        CoroutineScope(dispatcher).launch {
            runCatching {
                val ban = banRepository.get(targetPlayerUUID = event.connection.uniqueId)
                if (ban != null) {
                    event.setCancelReason(TextComponent()
                        .add("You are currently banned.\n\n") {
                            it.color = ChatColor.RED
                            it.isBold = true
                        }
                        .add("Reason: ") { it.color = ChatColor.GRAY }
                        .add(ban.reason ?: "No reason provided") { it.color = ChatColor.WHITE }
                        .add("\n")
                        .add("Expires: ") { it.color = ChatColor.GRAY }
                        .add((ban.expiresAt?.let { dateTimeFormatter.convert(it, FormatStyle.SHORT) } ?: "Never") + "\n\n") { it.color = ChatColor.WHITE }
                        .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }
                    )
                    event.isCancelled = true
                }

                val ip = Sanitizer().sanitizedIP(event.connection.socketAddress.toString())
                val ipBan = ipBanRepository.get(ip)
                if (ipBan != null) {
                    event.setCancelReason(TextComponent()
                        .add("Your are currently banned.\n\n") {
                            it.color = ChatColor.RED
                            it.isBold = true
                        }
                        .add("Reason: ") { it.color = ChatColor.GRAY }
                        .add(if (ipBan.reason.isEmpty()) "No reason provided" else ipBan.reason) { it.color = ChatColor.WHITE }
                        .add("\n")
                        .add("\n\n")
                        .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }
                    )
                    event.isCancelled = true
                }
            }
            .onFailure { throwable ->
                throwable.message?.let { logger.fatal(it) }
                throwable.printStackTrace()

                errorReporter.report(throwable)

                // If something goes wrong, better not to let players in
                event.setCancelReason(
                    TextComponent("An error occurred while contacting the PCB authentication server. Please try again later")
                )
                event.isCancelled = true
            }
            .also { event.completeIntent(plugin) }
        }
    }
}