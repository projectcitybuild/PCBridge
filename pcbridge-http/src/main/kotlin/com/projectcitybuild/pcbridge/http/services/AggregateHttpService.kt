package com.projectcitybuild.pcbridge.http.services

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.Aggregate
import retrofit2.Retrofit
import java.util.UUID

class AggregateHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(playerUUID: UUID, ip: String): Aggregate? {
        val response = responseParser.parse {
            retrofit.pcb().getAggregate(
                uuid = playerUUID.toString(),
                ip = ip,
            )
        }
        return response.data
    }
}