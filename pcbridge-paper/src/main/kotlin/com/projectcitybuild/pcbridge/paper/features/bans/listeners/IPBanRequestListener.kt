package com.projectcitybuild.pcbridge.paper.features.bans.listeners

import com.projectcitybuild.pcbridge.paper.core.logger.log
import com.projectcitybuild.pcbridge.paper.features.bans.events.IPBanRequestedEvent
import com.projectcitybuild.pcbridge.paper.features.bans.utilities.Sanitizer
import com.projectcitybuild.pcbridge.paper.features.bans.utilities.toMiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

/**
 * Receives an incoming ban from PCB and bans the offending
 * player if they're online
 */
class IPBanRequestListener(
    private val server: Server,
) : Listener {
    @EventHandler
    fun onBanRequested(event: IPBanRequestedEvent) {
        val matchingPlayer = server.onlinePlayers.firstOrNull {
            Sanitizer.sanitizedIP(it.address?.address.toString()) == Sanitizer.sanitizedIP(event.ban.ipAddress)
        }
        if (matchingPlayer == null) {
            log.info { "Skipping ban fulfillment, ip address (${event.ban.ipAddress}) not found" }
            return
        }

        log.info { "Player found matching ip address (${event.ban.ipAddress}), banning ${matchingPlayer.name}" }

        matchingPlayer.kick(
            event.ban.toMiniMessage(),
            PlayerKickEvent.Cause.IP_BANNED,
        )
    }
}