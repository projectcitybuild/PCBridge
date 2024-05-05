package com.projectcitybuild.pcbridge.http.services.mojang

import com.projectcitybuild.pcbridge.http.mojang
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.responses.MojangPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit

class PlayerUUIDHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class PlayerNotFoundException : Exception()

    suspend fun get(playerName: String, at: Long? = null): MojangPlayer? = withContext(Dispatchers.IO) {
        responseParser.parse {
            try {
                retrofit.mojang().getMojangPlayer(playerName)
            } catch (e: KotlinNullPointerException) {
                // Hacky workaround to catch 204 HTTP errors (username not found)
                throw PlayerNotFoundException()
            }
        }
    }
}