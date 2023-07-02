package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import java.util.UUID

class GenerateAccountVerificationURL(
    private val pcbClient: PCBClientFactory,
    private val apiClient: ResponseParser,
) {
    data class VerificationURL(val urlString: String)

    enum class FailureReason {
        ALREADY_LINKED,
        EMPTY_RESPONSE,
    }

    suspend fun generate(playerUUID: UUID): Result<VerificationURL, FailureReason> {
        try {
            val authApi = pcbClient.authAPI
            val response = apiClient.parse { authApi.getVerificationUrl(uuid = playerUUID.toString()) }
            val data = response.data

            return if (data == null || data.url.isEmpty()) {
                Failure(FailureReason.EMPTY_RESPONSE)
            } else {
                Success(VerificationURL(data.url))
            }
        } catch (e: ResponseParser.HTTPError) {
            if (e.errorBody?.id == "already_authenticated") {
                return Failure(FailureReason.ALREADY_LINKED)
            }
            throw e
        }
    }
}
