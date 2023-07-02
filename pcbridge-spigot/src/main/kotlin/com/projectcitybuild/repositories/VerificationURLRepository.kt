package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import java.util.UUID
import kotlin.jvm.Throws

class VerificationURLRepository(
    private val accountLinkHTTPService: AccountLinkHTTPService,
) {
    @Throws(AccountLinkHTTPService.AlreadyLinkedException::class)
    suspend fun generateVerificationURL(playerUUID: UUID): String? {
        return accountLinkHTTPService.generateVerificationURL(playerUUID)
    }
}