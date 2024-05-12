package com.projectcitybuild.pcbridge.features.telemetry.listeners

import com.projectcitybuild.pcbridge.features.telemetry.repositories.TelemetryRepository
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class TelemetryPlayerConnectListener(
    private val telemetryRepository: TelemetryRepository,
) : Listener {
    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent)
        = seen(event.player)

    @EventHandler
    suspend fun onPlayerQuit(event: PlayerQuitEvent)
        = seen(event.player)

    private suspend fun seen(player: Player) {
        telemetryRepository.playerSeen(
            playerUUID = player.uniqueId,
            playerName = player.name,
        )
    }
}
