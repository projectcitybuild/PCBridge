package com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Authorization
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class ConnectionHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun auth(
        playerUUID: UUID,
        ip: String?,
    ): Authorization = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().connectionAuth(
                uuid = playerUUID.toString(),
                ip = ip,
            )
        }
    }

    suspend fun end(
        playerUUID: UUID,
        sessionSeconds: Long,
    ) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().connectionEnd(
                uuid = playerUUID.toString(),
                sessionSeconds = sessionSeconds,
            )
        }
    }
}
