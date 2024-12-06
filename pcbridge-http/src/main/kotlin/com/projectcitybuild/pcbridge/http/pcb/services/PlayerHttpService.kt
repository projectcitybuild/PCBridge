package com.projectcitybuild.pcbridge.http.pcb.services

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.requests.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class PlayerHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun get(
        playerUUID: UUID,
        ip: String?,
    ): PlayerData =
        withContext(Dispatchers.IO) {
            responseParser.parse {
                retrofit.pcb().getPlayer(
                    uuid = playerUUID.toString(),
                    ip = ip,
                )
            }
        }
}
