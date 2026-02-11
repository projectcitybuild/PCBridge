package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerStats
import com.projectcitybuild.pcbridge.http.pcb.models.PlayersStatsRequest
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class StatsHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun increment(
        playerStats: PlayersStatsRequest,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().incrementStats(playerStats)
        }
    }
}
