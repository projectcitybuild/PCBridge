package com.projectcitybuild.features.aggregate

import com.projectcitybuild.entities.responses.Aggregate
import com.projectcitybuild.features.bans.Sanitizer
import com.projectcitybuild.repositories.AggregateRepository
import java.util.UUID

class GetAggregate(
    private val aggregateRepository: AggregateRepository,
) {
    suspend fun execute(playerUUID: UUID, ip: String): Aggregate {
        return aggregateRepository.get(
            playerUUID = playerUUID,
            ip = Sanitizer().sanitizedIP(ip),
        ) ?: Aggregate()
    }
}
