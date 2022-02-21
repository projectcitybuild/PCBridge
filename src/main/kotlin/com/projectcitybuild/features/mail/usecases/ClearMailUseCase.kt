package com.projectcitybuild.features.mail.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.mail.repositories.MailRepository
import java.lang.Integer.max
import java.util.*
import javax.inject.Inject

class ClearMailUseCase @Inject constructor(
    private val mailRepository: MailRepository,
) {
    enum class FailureReason {
        PAGE_TOO_HIGH,
        INVALID_PAGE_NUMBER,
    }

    fun clearMail(playerUUID: UUID, page: Int? = null): Result<Unit, FailureReason> {
        val totalUnreadCount = mailRepository.numberOfUncleared(playerUUID)

        if (page == null) {
            mailRepository.clearAll(playerUUID)
            return Success(Unit)
        }

        if (page > totalUnreadCount) {
            return Failure(FailureReason.PAGE_TOO_HIGH)
        }
        if (page < 1) {
            return Failure(FailureReason.INVALID_PAGE_NUMBER)
        }

        val mail = mailRepository.firstUncleared(
            playerUUID = playerUUID,
            offset = max(1, page - 1)
        )!!

        mailRepository.clear(mail.id)

        return Success(Unit)
    }
}