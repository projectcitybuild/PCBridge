package com.projectcitybuild.pcbridge.features.bans.repositories

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.services.pcb.AggregateHttpService
import java.util.UUID
import kotlin.jvm.Throws

class AggregateRepository(
    private val httpService: AggregateHttpService,
) {
    @Throws(Exception::class)
    suspend fun get(
        playerUUID: UUID,
        ip: String,
    ): Aggregate? {
        return httpService.get(
            playerUUID = playerUUID,
            ip = ip,
        )
    }
}
