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
    suspend fun grant(playerUUID: UUID, reason: String) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().opGrant(playerUUID.toString(), reason)
        }
    }

    suspend fun revoke(playerUUID: UUID) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.pcb().opRevoke(playerUUID.toString())
        }
    }
}
