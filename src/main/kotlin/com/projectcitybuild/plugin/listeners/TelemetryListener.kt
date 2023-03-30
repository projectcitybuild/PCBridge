package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.repositories.TelemetryRepository
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class TelemetryListener(
    private val telemetryRepository: TelemetryRepository,
) : SpigotListener {

    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent) =
        telemetryRepository.playerSeen(
            playerUUID = event.player.uniqueId,
            playerName = event.player.name,
        )

    @EventHandler
    suspend fun onPlayerLeave(event: PlayerQuitEvent) =
        telemetryRepository.playerSeen(
            playerUUID = event.player.uniqueId,
            playerName = event.player.name,
        )
}
