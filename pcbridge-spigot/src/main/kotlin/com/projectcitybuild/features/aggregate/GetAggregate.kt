package com.projectcitybuild.features.aggregate

import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.repositories.AggregateRepository
import java.util.UUID
import kotlin.jvm.Throws

class GetAggregate(
    private val aggregateRepository: AggregateRepository,
) {
    @Throws(Exception::class)
    suspend fun execute(playerUUID: UUID, ip: String): Aggregate {
        return aggregateRepository.get(
            playerUUID = playerUUID,
            ip = Sanitizer().sanitizedIP(ip),
        ) ?: Aggregate()
    }
}
