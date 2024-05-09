package com.projectcitybuild.features.sync.actions

import com.projectcitybuild.features.sync.repositories.SyncRepository
import com.projectcitybuild.utils.Failure
import com.projectcitybuild.utils.Result
import com.projectcitybuild.utils.Success
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import java.util.UUID

class GenerateAccountVerificationURL(
    private val syncRepository: SyncRepository,
) {
    data class VerificationURL(val urlString: String)

    enum class FailureReason {
        ALREADY_LINKED,
        EMPTY_RESPONSE,
    }

    suspend fun generate(playerUUID: UUID): Result<VerificationURL, FailureReason> {
        return try {
            val url = syncRepository.generateVerificationURL(playerUUID)

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
