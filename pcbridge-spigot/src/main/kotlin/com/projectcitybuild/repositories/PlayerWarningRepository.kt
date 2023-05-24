package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.entities.responses.PlayerWarning
import java.util.UUID

class PlayerWarningRepository(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    suspend fun get(playerUUID: UUID, playerName: String): List<PlayerWarning> {
        val response = apiClient.execute {
            apiRequestFactory.pcb.warningAPI.get(
                bannedPlayerId = playerUUID.toString(),
                bannedPlayerAlias = playerName,
            )
        }
        return response.data ?: listOf()
    }

    suspend fun create(
        warnedPlayerUUID: UUID,
        warnedPlayerName: String,
        warnerPlayerUUID: UUID,
        warnerPlayerName: String,
        reason: String,
    ): PlayerWarning? {
        val response = apiClient.execute {
            apiRequestFactory.pcb.warningAPI.create(
                warnedPlayerId = warnedPlayerUUID.toString(),
                warnedPlayerAlias = warnedPlayerName,
                warnerPlayerId = warnerPlayerUUID.toString(),
                warnerPlayerAlias = warnerPlayerName,
                reason = reason,
            )
        }
        return response.data
    }

    suspend fun acknowledge(warningId: Int): PlayerWarning? {
        val response = apiClient.execute {
            apiRequestFactory.pcb.warningAPI.acknowledge(
                warningId = warningId,
            )
        }
        return response.data
    }
}
