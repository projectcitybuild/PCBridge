package com.projectcitybuild.pcbridge.paper.features.bans.listeners

import com.projectcitybuild.pcbridge.paper.architecture.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities.SpigotSanitizer
import com.projectcitybuild.pcbridge.paper.features.bans.utilities.toMiniMessage
import com.projectcitybuild.pcbridge.webserver.data.IPBanRequestedWebhook
import com.projectcitybuild.pcbridge.webserver.data.UUIDBanRequestedWebhook
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent

/**
 * Receives an incoming ban from PCB and bans the offending
 * player if they're online
 */
class BanWebhookListener(
    private val server: Server,
) : Listener {
    @EventHandler
    fun onIpBanned(event: WebhookReceivedEvent) {
        if (event.webhook !is IPBanRequestedWebhook) return

        val ban = event.webhook.ban
        val matchingPlayer = server.onlinePlayers.firstOrNull {
            SpigotSanitizer.ipAddress(it.address?.address.toString()) == SpigotSanitizer.ipAddress(ban.ipAddress)
        }
        if (matchingPlayer == null) {
            log.info { "Skipping ban fulfillment, ip address (${ban.ipAddress}) not found" }
            return
        }

        log.info { "Player found matching ip address (${ban.ipAddress}), banning ${matchingPlayer.name}" }

        matchingPlayer.kick(
            ban.toMiniMessage(),
            PlayerKickEvent.Cause.IP_BANNED,
        )
        server.broadcast(
            MiniMessage.miniMessage().deserialize("<gray>${matchingPlayer.name} has been banned</gray>")
        )
    }

    @EventHandler
    fun onUuidBanned(event: WebhookReceivedEvent) {
        if (event.webhook !is UUIDBanRequestedWebhook) return

        val ban = event.webhook.ban
        val bannedPlayer = ban.bannedPlayer
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
            ban.toMiniMessage(),
            PlayerKickEvent.Cause.BANNED,
        )
        server.broadcast(
            MiniMessage.miniMessage().deserialize("<gray>${matchingPlayer.name} has been banned</gray>")
        )
    }
}