package com.projectcitybuild.modules.telemetry.listeners

import com.projectcitybuild.repositories.TelemetryRepository
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(
    private val telemetryRepository: TelemetryRepository,
) : SpigotListener<PlayerQuitEvent> {

    override suspend fun handle(event: PlayerQuitEvent) {
        telemetryRepository.playerSeen(
            playerUUID = event.player.uniqueId,
            playerName = event.player.name,
        )
    }
}
