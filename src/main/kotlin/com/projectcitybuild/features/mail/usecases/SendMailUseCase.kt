package com.projectcitybuild.features.mail.usecases

import com.projectcitybuild.features.mail.repositories.MailRepository
import java.util.*
import javax.inject.Inject

class SendMailUseCase @Inject constructor(
    private val mailRepository: MailRepository,
) {
    fun sendMail(
        senderUUID: UUID,
        senderName: String,
        targetUUID: UUID,
        targetName: String,
        message: String,
    ) {
        mailRepository.send(
            senderUUID,
            senderName,
            targetUUID,
            targetName,
            message,
        )
    }
}