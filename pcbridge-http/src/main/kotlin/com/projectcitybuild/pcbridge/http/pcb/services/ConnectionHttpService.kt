package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.pcb.models.Authorization
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
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
