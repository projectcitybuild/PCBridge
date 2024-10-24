package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.PlayerData
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
        ip: String,
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
