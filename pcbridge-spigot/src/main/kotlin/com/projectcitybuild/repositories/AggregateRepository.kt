package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import java.util.UUID

class AggregateRepository(
    private val pcbClient: PCBClient,
    private val apiClient: APIClient,
) {
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? {
        val aggregate = apiClient.execute {
            pcbClient.aggregateAPI.get(
                uuid = playerUUID.toString(),
                ip = ip,
            )
        }
        return aggregate.data
    }
}
