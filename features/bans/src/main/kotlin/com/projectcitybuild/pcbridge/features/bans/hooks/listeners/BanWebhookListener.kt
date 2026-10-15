package com.projectcitybuild.pcbridge.features.bans.hooks.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.core.support.java.uuidFromUnsanitizedString
import com.projectcitybuild.pcbridge.features.bans.bansTracer
import com.projectcitybuild.pcbridge.features.bans.domain.utilities.toMiniMessage
import com.projectcitybuild.pcbridge.platform.paper.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.platform.paper.utilities.SpigotSanitizer
import com.projectcitybuild.pcbridge.runtime.listeners.scopedSync
import com.projectcitybuild.pcbridge.runtime.webhooks.events.WebhookReceivedEvent
import com.projectcitybuild.pcbridge.platform.webserver.data.WebhookEvent
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
    fun onIpBanned(event: WebhookReceivedEvent) =
        event.scopedSync(bansTracer, this::class.java) {
            val webhook = event.webhook
            if (webhook !is WebhookEvent.IPBanRequested) return@scopedSync

            val ban = webhook.ban
            val matchingPlayer =
                server.onlinePlayers.firstOrNull {
                    SpigotSanitizer.ipAddress(it.address?.address.toString()) == SpigotSanitizer.ipAddress(ban.ipAddress)
                }
            if (matchingPlayer == null) {
                logSync.info { "Skipping ban fulfillment, ip address (${ban.ipAddress}) not found" }
                return@scopedSync
            }

            logSync.info { "Player found matching ip address (${ban.ipAddress}), banning ${matchingPlayer.name}" }

            matchingPlayer.kick(
                ban.toMiniMessage(),
                PlayerKickEvent.Cause.IP_BANNED,
            )
            server.broadcast(
                MiniMessage.miniMessage().deserialize("<gray>${matchingPlayer.name} has been banned</gray>"),
            )
        }

    @EventHandler
    fun onUuidBanned(event: WebhookReceivedEvent) =
        event.scopedSync(bansTracer, this::class.java) {
            val webhook = event.webhook
            if (webhook !is WebhookEvent.UUIDBanRequested) return@scopedSync

            val ban = webhook.ban
            val bannedPlayer = ban.bannedPlayer
            if (bannedPlayer == null) {
                logSync.info { "Skipping ban fulfillment, ban had no player data" }
                return@scopedSync
            }
            val bannedUuid = uuidFromUnsanitizedString(bannedPlayer.uuid)
            val matchingPlayer = server.onlinePlayer(uuid = bannedUuid)
            if (matchingPlayer == null) {
                logSync.info { "Skipping ban fulfillment, player (${bannedPlayer.uuid}) not found" }
                return@scopedSync
            }

            logSync.info { "Player found, banning ${matchingPlayer.uniqueId}" }

            matchingPlayer.kick(
                ban.toMiniMessage(),
                PlayerKickEvent.Cause.BANNED,
            )
            server.broadcast(
                MiniMessage.miniMessage().deserialize("<gray>${matchingPlayer.name} has been banned</gray>"),
            )
        }
}
