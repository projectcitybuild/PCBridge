package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import java.util.UUID

class TelemetryRepository(
    private val pcbClient: PCBClient,
    private val apiClient: APIClient,
) {
    suspend fun playerSeen(playerUUID: UUID, playerName: String) {
        val telemetryAPI = pcbClient.telemetryAPI
        apiClient.execute {
            telemetryAPI.seen(
                playerUUID = playerUUID.toString(),
                playerName = playerName,
            )
        }
    }
}
