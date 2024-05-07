package com.projectcitybuild.features.warnings.repositories

import com.projectcitybuild.pcbridge.http.responses.PlayerWarning
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerWarningHttpService
import java.util.UUID

class PlayerWarningRepository(
    private val playerWarningHttpService: PlayerWarningHttpService,
) {
    suspend fun get(playerUUID: UUID, playerName: String): List<PlayerWarning> {
        return playerWarningHttpService.get(
            playerUUID = playerUUID,
            playerName = playerName,
        )
    }

    suspend fun acknowledge(warningId: Int): PlayerWarning? {
        return playerWarningHttpService.acknowledge(warningId)
    }
}
