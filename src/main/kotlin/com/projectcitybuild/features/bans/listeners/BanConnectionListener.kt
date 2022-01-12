package com.projectcitybuild.features.bans.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.features.bans.repositories.BanRepository
import com.projectcitybuild.platforms.bungeecord.extensions.add
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BanConnectionListener(
    private val plugin: Plugin,
    private val banRepository: BanRepository,
    private val logger: LoggerProvider
) : BungeecordListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: LoginEvent) {
        event.registerIntent(plugin)

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val ban = banRepository.get(targetPlayerUUID = event.connection.uniqueId)
                if (ban != null) {
                    event.setCancelReason(TextComponent()
                        .add("You are currently banned.\n\n") {
                            it.color = ChatColor.RED
                            it.isBold = true
                        }
                        .add("Reason: ") { it.color = ChatColor.GRAY }
                        .add((ban.reason ?: "No reason provided") + "\n") { it.color = ChatColor.WHITE }
                        .add("Expires: ") { it.color = ChatColor.GRAY }
                        .add(ban.expiresAt?.toString() ?: ("Never" + "\n\n")) { it.color = ChatColor.WHITE }
                        .add("Appeal @ https://projectcitybuild.com") { it.color = ChatColor.AQUA }
                    )
                    event.isCancelled = true
                }
            }
            .onFailure { throwable ->
                throwable.message?.let { logger.fatal(it) }
                throwable.printStackTrace()

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