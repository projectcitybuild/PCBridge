package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.entities.responses.Aggregate
import java.util.UUID

class AggregateRepository(
    private val apiClient: APIClient,
    private val apiRequestFactory: APIRequestFactory,
) {
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? {
        val aggregate = apiClient.execute {
            apiRequestFactory.pcb.aggregateAPI.get(
                uuid = playerUUID.toString(),
                ip = ip,
            )
        }
        return aggregate.data
    }
}
