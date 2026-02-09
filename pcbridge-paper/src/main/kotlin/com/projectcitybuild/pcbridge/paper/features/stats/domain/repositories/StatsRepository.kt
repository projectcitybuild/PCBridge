package com.projectcitybuild.pcbridge.paper.features.stats.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.services.StatsHttpService
import java.util.UUID

class StatsRepository(
    private val statsHttpService: StatsHttpService,
) {
    suspend fun afkTime(
        uuid: UUID,
        time: Long,
    ) = statsHttpService.afkTime(
        uuid = uuid,
        time = time,
    )

    suspend fun blocks(
        uuid: UUID,
        placed: Long,
        destroyed: Long,
    ) = statsHttpService.blocks(
        uuid = uuid,
        placed = placed,
        destroyed = destroyed,
    )

    suspend fun movement(
        uuid: UUID,
        travelled: Long,
    ) = statsHttpService.movement(
        uuid = uuid,
        travelled = travelled,
    )
}