package com.projectcitybuild.pcbridge.paper.features.stats.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.http.pcb.models.PlayersStatsRequest
import com.projectcitybuild.pcbridge.http.pcb.services.StatsHttpService
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.uuid
import java.util.UUID

class StatsRepository(
    private val statsHttpService: StatsHttpService,
) {
    suspend fun increment(
        playerStats: Map<String, PlayerStats>,
    ) = statsHttpService.increment(
        PlayersStatsRequest(playerStats)
    )
}