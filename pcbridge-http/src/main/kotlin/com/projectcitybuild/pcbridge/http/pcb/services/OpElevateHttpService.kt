package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class OpElevateHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun start(playerUUID: UUID, reason: String) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().opStart(playerUUID.toString(), reason)
        }
    }

    suspend fun end(playerUUID: UUID) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().opEnd(playerUUID.toString())
        }
    }
}
