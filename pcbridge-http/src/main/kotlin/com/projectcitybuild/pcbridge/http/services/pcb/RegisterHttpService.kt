package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class RegisterHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    suspend fun sendCode(playerUUID: UUID, playerAlias: String, email: String) =
        withContext(Dispatchers.IO) {
            responseParser.parse {
                retrofit.pcb().sendRegisterCode(
                    uuid = playerUUID.toString(),
                    playerAlias = playerAlias,
                    email = email,
                )
            }
        }

    suspend fun verifyCode(playerUUID: UUID, code: String) =
        withContext(Dispatchers.IO) {
            responseParser.parse {
                retrofit.pcb().verifyRegisterCode(
                    uuid = playerUUID.toString(),
                    code = code,
                )
            }
        }
}
