package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.services.AggregateHttpService
import java.util.UUID

class AggregateRepository(
    private val aggregateHttpService: AggregateHttpService,
) {
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? {
        return aggregateHttpService.get(
            playerUUID = playerUUID,
            ip = ip,
        )
    }
}
