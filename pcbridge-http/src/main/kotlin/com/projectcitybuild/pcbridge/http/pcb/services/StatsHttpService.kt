package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class StatsHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun afkTime(
        uuid: UUID,
        time: Long,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().incrementStats(
                uuid = uuid.toString(),
                afkTime = time,
            )
        }
    }

    suspend fun blocks(
        uuid: UUID,
        placed: Long,
        destroyed: Long,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().incrementStats(
                uuid = uuid.toString(),
                blocksPlaced = placed,
                blocksDestroyed = destroyed,
            )
        }
    }

    suspend fun movement(
        uuid: UUID,
        travelled: Long,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().incrementStats(
                uuid = uuid.toString(),
                blocksTravelled = travelled,
            )
        }
    }
}
