package com.projectcitybuild.features.ranksync.usecases

import com.projectcitybuild.pcbridge.core.utils.Failure
import com.projectcitybuild.pcbridge.core.utils.Result
import com.projectcitybuild.pcbridge.core.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import com.projectcitybuild.repositories.VerificationURLRepository
import java.util.UUID

class GenerateAccountVerificationURL(
    private val verificationURLRepository: VerificationURLRepository
) {
    data class VerificationURL(val urlString: String)

    enum class FailureReason {
        ALREADY_LINKED,
        EMPTY_RESPONSE,
    }

    suspend fun generate(playerUUID: UUID): Result<VerificationURL, FailureReason> {
        return try {
            val url = verificationURLRepository.generateVerificationURL(playerUUID)

            return if (url.isNullOrEmpty()) {
                Failure(FailureReason.EMPTY_RESPONSE)
            } else {
                Success(VerificationURL(url))
            }
        } catch (e: AccountLinkHTTPService.AlreadyLinkedException) {
            Failure(FailureReason.ALREADY_LINKED)
        }
    }
}
