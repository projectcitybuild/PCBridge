package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import com.projectcitybuild.pcbridge.http.responses.PlayerWarning
import java.util.UUID

class PlayerWarningRepository(
    private val pcbClient: PCBClient,
    private val apiClient: APIClient,
) {
    suspend fun get(playerUUID: UUID, playerName: String): List<PlayerWarning> {
        val response = apiClient.execute {
            pcbClient.warningAPI.get(
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
            pcbClient.warningAPI.create(
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
            pcbClient.warningAPI.acknowledge(
                warningId = warningId,
            )
        }
        return response.data
    }
}
