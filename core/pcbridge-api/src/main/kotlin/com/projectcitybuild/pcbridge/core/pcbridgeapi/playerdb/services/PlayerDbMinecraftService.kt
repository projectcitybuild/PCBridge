package com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb.services

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb.requests.playerDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class PlayerDbMinecraftService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun player(name: String) = withContext(Dispatchers.IO) {
        responseParser.parse {
            retrofit.playerDb().getPlayer(name)
        }
    }
}
