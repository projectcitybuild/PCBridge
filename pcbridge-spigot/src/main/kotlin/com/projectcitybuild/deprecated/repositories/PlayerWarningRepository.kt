package com.projectcitybuild.repositories

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

    suspend fun create(
        warnedPlayerUUID: UUID,
        warnedPlayerName: String,
        warnerPlayerUUID: UUID,
        warnerPlayerName: String,
        reason: String,
    ): PlayerWarning? {
        return playerWarningHttpService.create(
            warnedPlayerUUID = warnedPlayerUUID,
            warnedPlayerName = warnedPlayerName,
            warnerPlayerUUID = warnerPlayerUUID,
            warnerPlayerName = warnerPlayerName,
            reason = reason,
        )
    }

    suspend fun acknowledge(warningId: Int): PlayerWarning? {
        return playerWarningHttpService.acknowledge(warningId)
    }
}
