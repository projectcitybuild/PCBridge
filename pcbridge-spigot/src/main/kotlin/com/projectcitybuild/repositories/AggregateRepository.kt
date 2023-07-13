package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.services.pcb.AggregateHttpService
import java.util.UUID
import kotlin.jvm.Throws

class AggregateRepository(
    private val aggregateHttpService: AggregateHttpService,
) {
    @Throws(Exception::class)
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? {
        return aggregateHttpService.get(
            playerUUID = playerUUID,
            ip = ip,
        )
    }
}
