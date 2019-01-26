package com.projectcitybuild.spigot.modules.bans.listeners

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.environment.RawColor
import com.projectcitybuild.spigot.environment.RawFormat
import com.projectcitybuild.actions.CheckBanStatusAction
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BanConnectionListener : Listenable<AsyncPlayerPreLoginEvent> {
    override var environment: Environment? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    override fun observe(event: AsyncPlayerPreLoginEvent) {
        val environment = environment ?: throw Exception("Environment is null")

        val action = CheckBanStatusAction(environment)
        val result = action.execute(playerId = event.uniqueId)

        if (result is CheckBanStatusAction.Result.SUCCESS && result.ban != null) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    """
                        ${RawColor.RED}${RawFormat.BOLD}You are currently banned.${RawFormat.RESET}

                        ${RawColor.GRAY}Reason: ${RawColor.WHITE}${result.ban.reason}
                        ${RawColor.GRAY}Expires: ${RawColor.WHITE}${result.ban.expiresAt ?: "Never"}

                        ${RawColor.AQUA}Appeal @ https://projectcitybuild.com
                    """.trimIndent()
            )
        }
    }
}