package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID
import kotlin.jvm.Throws

class AccountLinkHTTPService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class AlreadyLinkedException : Exception()

    @Throws(AlreadyLinkedException::class)
    suspend fun generateVerificationURL(playerUUID: UUID): String? =
        withContext(Dispatchers.IO) {
            try {
                val response =
                    responseParser.parse {
                        retrofit.pcb().getVerificationUrl(uuid = playerUUID.toString())
                    }
                val data = response.data

                if (data == null || data.url.isEmpty()) {
                    null
                } else {
                    data.url
                }
            } catch (e: ResponseParser.HTTPError) {
                if (e.errorBody?.id == "already_authenticated") {
                    throw AlreadyLinkedException()
                }
                throw e
            }
        }
}
