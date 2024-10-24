package com.projectcitybuild.pcbridge.features.bans.listeners

import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.features.bans.events.UUIDBanRequestedEvent
import com.projectcitybuild.pcbridge.features.bans.utilities.toMiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

/**
 * Receives an incoming ban from PCB and bans the offending
 * player if they're online
 */
class UUIDBanRequestListener(
    private val server: Server,
) : Listener {
    @EventHandler
    fun onBanRequested(event: UUIDBanRequestedEvent) {
        val bannedPlayer = event.ban.bannedPlayer
        if (bannedPlayer == null) {
            log.info { "Skipping ban fulfillment, ban had no player data" }
            return
        }
        val matchingPlayer = server.onlinePlayers.firstOrNull {
            it.uniqueId.toString().replace("-", "") == bannedPlayer.uuid
        }
        if (matchingPlayer == null) {
            log.info { "Skipping ban fulfillment, player (${bannedPlayer.uuid}) not found" }
            return
        }

        log.info { "Player found, banning ${matchingPlayer.uniqueId}" }

        matchingPlayer.kick(
            event.ban.toMiniMessage(),
            PlayerKickEvent.Cause.BANNED,
        )
    }
}