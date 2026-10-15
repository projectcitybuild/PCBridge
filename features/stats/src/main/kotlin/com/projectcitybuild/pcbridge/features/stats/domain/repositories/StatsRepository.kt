package com.projectcitybuild.pcbridge.features.stats.domain.repositories

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayersStatsRequest
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.StatsHttpService
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