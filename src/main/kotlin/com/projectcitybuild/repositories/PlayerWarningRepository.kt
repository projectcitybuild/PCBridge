package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.entities.responses.PlayerWarning
import java.util.UUID
import javax.inject.Inject

class PlayerWarningRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    suspend fun get(playerUUID: UUID, playerName: String) {
        apiClient.execute {
            apiRequestFactory.pcb.warningAPI.get(
                bannedPlayerId = playerUUID.toString(),
                bannedPlayerAlias = playerName,
            )
        }
    }

    suspend fun create(
        warnedPlayerUUID: UUID,
        warnedPlayerName: String,
        warnerPlayerUUID: UUID,
        warnerPlayerName: String,
        reason: String,
    ) {
        apiClient.execute {
            apiRequestFactory.pcb.warningAPI.create(
                warnedPlayerId = warnedPlayerUUID.toString(),
                warnedPlayerAlias = warnedPlayerName,
                warnerPlayerId = warnerPlayerUUID.toString(),
                warnerPlayerAlias = warnerPlayerName,
                reason = reason,
            )
        }
    }

    suspend fun acknowledge(warning: PlayerWarning) {
        apiClient.execute {
            apiRequestFactory.pcb.warningAPI.acknowledge(
                warningId = warning.id,
            )
        }
    }
}
