package com.projectcitybuild.features.mail.usecases

import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Result
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.entities.Mail
import com.projectcitybuild.features.mail.repositories.MailRepository
import com.projectcitybuild.modules.datetime.formatter.DateTimeFormatter
import java.lang.Integer.max
import java.util.*
import javax.inject.Inject

class GetUnclearedMailUseCase @Inject constructor(
    private val mailRepository: MailRepository,
    private val dateTimeFormatter: DateTimeFormatter,
) {
    enum class FailureReason {
        NO_MAIL,
        PAGE_TOO_HIGH,
        INVALID_PAGE_NUMBER,
    }

    data class UnreadMail(
        val totalCount: Int,
        val mail: Mail,
        val formattedSendDate: String,
    )

    fun getMail(playerUUID: UUID, page: Int): Result<UnreadMail, FailureReason> {
        val totalUnreadCount = mailRepository.numberOfUncleared(playerUUID)

        if (totalUnreadCount == 0) {
            return Failure(FailureReason.NO_MAIL)
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

        mailRepository.markAsRead(mail.id)

        return Success(
            UnreadMail(
                totalUnreadCount,
                mail,
                formattedSendDate = dateTimeFormatter.convert(mail.createdAt),
            )
        )
    }
}