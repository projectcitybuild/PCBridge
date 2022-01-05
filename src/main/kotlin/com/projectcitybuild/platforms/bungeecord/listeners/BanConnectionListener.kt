package com.projectcitybuild.platforms.bungeecord.listeners

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.entities.Success
import com.projectcitybuild.entities.models.GameBan
import com.projectcitybuild.modules.bans.BanRepository
import com.projectcitybuild.platforms.spigot.extensions.add
import kotlinx.coroutines.runBlocking
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.LoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority

class BanConnectionListener(
    private val banRepository: BanRepository,
    private val logger: LoggerProvider
) : Listener {

    // This event is actually asynchronous, so despite the naming
    // this should be blocking
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPreLoginEvent(event: LoginEvent) = runBlocking {
        runCatching {
            banRepository.get(targetPlayerUUID = event.connection.uniqueId)?.let { ban ->
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
        }.onFailure { throwable ->
            throwable.message?.let { logger.fatal(it) }
            throwable.printStackTrace()
        }
    }
}