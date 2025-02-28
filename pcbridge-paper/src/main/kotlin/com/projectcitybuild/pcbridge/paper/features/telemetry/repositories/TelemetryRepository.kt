package com.projectcitybuild.pcbridge.paper.features.telemetry.repositories

import com.projectcitybuild.pcbridge.http.pcb.services.TelemetryHttpService
import com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities.sanitized
import java.net.InetAddress
import java.util.UUID

class TelemetryRepository(
    private val telemetryHttpService: TelemetryHttpService,
) {
    suspend fun playerSeen(
        playerUUID: UUID,
        playerName: String,
        ip: InetAddress?,
    ) {
        telemetryHttpService.playerSeen(
            playerUUID = playerUUID,
            playerName = playerName,
            ip = ip?.sanitized(),
        )
    }
}
