package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID
import kotlin.jvm.Throws

class RegisterHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class AlreadyLinkedException : Exception()

    @Throws(AlreadyLinkedException::class)
    suspend fun sendCode(playerUUID: UUID, playerAlias: String, email: String) =
        withContext(Dispatchers.IO) {
            try {
                responseParser.parse {
                    retrofit.pcb().sendRegisterCode(
                        uuid = playerUUID.toString(),
                        playerName = playerAlias,
                        email = email,
                    )
                }
            } catch (e: ResponseParser.HTTPError) {
                if (e.errorBody?.id == "already_authenticated") {
                    throw AlreadyLinkedException()
                }
                throw e
            }
        }
}
