package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import java.util.UUID
import javax.inject.Inject

class TelemetryRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    suspend fun playerSeen(playerUUID: UUID, playerName: String) {
        val telemetryAPI = apiRequestFactory.pcb.telemetryAPI
        apiClient.execute {
            telemetryAPI.seen(
                playerUUID = playerUUID.toString(),
                playerName = playerName,
            )
        }
    }
}
