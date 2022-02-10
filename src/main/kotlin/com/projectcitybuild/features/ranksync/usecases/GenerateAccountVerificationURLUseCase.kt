package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import java.util.*
import javax.inject.Inject

class GenerateAccountVerificationURLUseCase @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    data class VerificationURL(val urlString: String)

    enum class FailureReason {
        ALREADY_LINKED,
        EMPTY_RESPONSE,
    }

    suspend fun generate(playerUUID: UUID): Result<VerificationURL, FailureReason> {
        try {
            val authApi = apiRequestFactory.pcb.authApi
            val response = apiClient.execute { authApi.getVerificationUrl(uuid = playerUUID.toString()) }

            return if (response.data == null) {
                Failure(FailureReason.EMPTY_RESPONSE)
            } else {
                Success(VerificationURL(response.data.url))
            }

        } catch (throwable: APIClient.HTTPError) {
            if (throwable.errorBody?.id == "already_authenticated") {
                return Failure(FailureReason.ALREADY_LINKED)
            }
            throw throwable
        }
    }
}