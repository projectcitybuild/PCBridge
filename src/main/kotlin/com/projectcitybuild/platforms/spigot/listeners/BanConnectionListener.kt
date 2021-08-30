package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.platforms.spigot.environment.RawColor
import com.projectcitybuild.platforms.spigot.environment.RawFormat
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BanConnectionListener(
        private val apiRequestFactory: APIRequestFactory
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun observe(event: AsyncPlayerPreLoginEvent) {
        val action = CheckBanStatusAction(apiRequestFactory)
        val result = action.execute(playerId = event.uniqueId)

        if (result is CheckBanStatusAction.Result.SUCCESS && result.ban != null) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    """
                        ${RawColor.RED}${RawFormat.BOLD}You are currently banned.${RawFormat.RESET}

                        ${RawColor.GRAY}Reason: ${RawColor.WHITE}${result.ban.reason ?: "No reason provided"}
                        ${RawColor.GRAY}Expires: ${RawColor.WHITE}${result.ban.expiresAt ?: "Never"}

                        ${RawColor.AQUA}Appeal @ https://projectcitybuild.com
                    """.trimIndent()
            )
        }
    }
}