package com.projectcitybuild.pcbridge.http.services

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.PlayerWarning
import retrofit2.Retrofit
import java.util.UUID

class PlayerWarningHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(
        playerUUID: UUID,
        playerName: String
    ): List<PlayerWarning> {
        val response = responseParser.parse {
            retrofit.pcb().getWarnings(
                playerId = playerUUID.toString(),
                playerAlias = playerName,
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
        val response = responseParser.parse {
            retrofit.pcb().createWarning(
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
        val response = responseParser.parse {
            retrofit.pcb().acknowledgeWarning(
                warningId = warningId,
            )
        }
        return response.data
    }
}