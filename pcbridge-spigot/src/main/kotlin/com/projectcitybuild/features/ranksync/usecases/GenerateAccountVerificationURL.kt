package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import java.util.UUID

class GenerateAccountVerificationURL(
    private val pcbClient: PCBClient,
    private val apiClient: APIClient,
) {
    data class VerificationURL(val urlString: String)

    enum class FailureReason {
        ALREADY_LINKED,
        EMPTY_RESPONSE,
    }

    suspend fun generate(playerUUID: UUID): Result<VerificationURL, FailureReason> {
        try {
            val authApi = pcbClient.authAPI
            val response = apiClient.execute { authApi.getVerificationUrl(uuid = playerUUID.toString()) }
            val data = response.data

            return if (data == null || data.url.isEmpty()) {
                Failure(FailureReason.EMPTY_RESPONSE)
            } else {
                Success(VerificationURL(data.url))
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "already_authenticated") {
                return Failure(FailureReason.ALREADY_LINKED)
            }
            throw e
        }
    }
}
