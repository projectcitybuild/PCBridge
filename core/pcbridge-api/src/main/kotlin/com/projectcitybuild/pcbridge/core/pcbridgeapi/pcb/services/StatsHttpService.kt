package com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayersStatsRequest
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.requests.pcb
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
