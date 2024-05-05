package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class AggregateHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? = withContext(Dispatchers.IO) {
        val response = responseParser.parse {
            retrofit.pcb().getAggregate(
                uuid = playerUUID.toString(),
                ip = ip,
            )
        }
        response.data
    }
}