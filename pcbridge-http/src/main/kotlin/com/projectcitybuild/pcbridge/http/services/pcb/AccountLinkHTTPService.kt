package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import retrofit2.Retrofit
import java.util.UUID

class AccountLinkHTTPService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class AlreadyLinkedException: Exception()

    suspend fun generateVerificationURL(playerUUID: UUID): String? {
        try {
            val response = responseParser.parse {
                retrofit.pcb().getVerificationUrl(uuid = playerUUID.toString())
            }
            val data = response.data

            return if (data == null || data.url.isEmpty()) {
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