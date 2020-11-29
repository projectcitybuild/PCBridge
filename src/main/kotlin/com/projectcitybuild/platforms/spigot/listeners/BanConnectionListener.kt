package com.projectcitybuild.platforms.spigot.listeners

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.NetworkClients
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BanConnectionListener(
        private val environment: EnvironmentProvider,
        private val networkClients: NetworkClients
): Listenable<AsyncPlayerPreLoginEvent> {

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerPreLoginEvent) {
        val action = CheckBanStatusAction(networkClients)
        val result = action.execute(playerId = event.uniqueId)

        if (result is CheckBanStatusAction.Result.SUCCESS && result.ban != null) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    """
                        ${com.projectcitybuild.platforms.spigot.environment.RawColor.RED}${com.projectcitybuild.platforms.spigot.environment.RawFormat.BOLD}You are currently banned.${com.projectcitybuild.platforms.spigot.environment.RawFormat.RESET}

                        ${com.projectcitybuild.platforms.spigot.environment.RawColor.GRAY}Reason: ${com.projectcitybuild.platforms.spigot.environment.RawColor.WHITE}${result.ban.reason ?: "No reason provided"}
                        ${com.projectcitybuild.platforms.spigot.environment.RawColor.GRAY}Expires: ${com.projectcitybuild.platforms.spigot.environment.RawColor.WHITE}${result.ban.expiresAt ?: "Never"}

                        ${com.projectcitybuild.platforms.spigot.environment.RawColor.AQUA}Appeal @ https://projectcitybuild.com
                    """.trimIndent()
            )
        }
    }
}