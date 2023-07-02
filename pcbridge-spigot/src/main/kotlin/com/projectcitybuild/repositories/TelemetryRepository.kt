package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.services.pcb.TelemetryHttpService
import java.util.UUID

class TelemetryRepository(
    private val telemetryHttpService: TelemetryHttpService,
) {
    suspend fun playerSeen(playerUUID: UUID, playerName: String) {
        telemetryHttpService.playerSeen(
            playerUUID = playerUUID,
            playerName = playerName,
        )
    }
}
