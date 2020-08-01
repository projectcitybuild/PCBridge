package com.projectcitybuild.spigot.modules.bans.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.environment.RawColor
import com.projectcitybuild.spigot.environment.RawFormat
import com.projectcitybuild.actions.CheckBanStatusAction
import com.projectcitybuild.api.APIProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BanConnectionListener(
        private val environment: EnvironmentProvider,
        private val apiProvider: APIProvider
): Listenable<AsyncPlayerPreLoginEvent> {

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerPreLoginEvent) {
        val action = CheckBanStatusAction(environment, apiProvider)
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