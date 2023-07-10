package com.projectcitybuild.modules.telemetry.listeners

import com.projectcitybuild.repositories.TelemetryRepository
import com.projectcitybuild.support.spigot.listeners.SpigotListener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(
    private val telemetryRepository: TelemetryRepository,
) : SpigotListener<PlayerJoinEvent> {

    override suspend fun handle(event: PlayerJoinEvent) {
        telemetryRepository.playerSeen(
            playerUUID = event.player.uniqueId,
            playerName = event.player.name,
        )
    }
}
