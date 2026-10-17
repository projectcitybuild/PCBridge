package com.projectcitybuild.pcbridge.features.teleportation.hooks.listeners

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.features.teleportation.services.TeleportRequestService
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class TeleportRequestListener(
    private val server: Server,
    private val requestService: TeleportRequestService,
) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        requestService.disconnect(event.player.uniqueId).forEach { request ->
            if (request.requesterId == event.player.uniqueId) {
                server.getPlayer(request.targetId)?.sendMessage(
                    l10n.teleportRequestDisconnected(request.requesterName),
                )
            } else {
                server.getPlayer(request.requesterId)?.sendMessage(
                    l10n.teleportRequestDisconnected(request.targetName),
                )
            }
        }
    }
}
