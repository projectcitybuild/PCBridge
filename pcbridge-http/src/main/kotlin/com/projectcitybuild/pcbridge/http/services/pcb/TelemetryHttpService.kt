package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import retrofit2.Retrofit
import java.util.UUID

class TelemetryHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun playerSeen(playerUUID: UUID, playerName: String) {
        responseParser.parse {
            retrofit.pcb().telemetrySeen(
                playerUUID = playerUUID.toString(),
                playerName = playerName,
            )
        }
    }
}