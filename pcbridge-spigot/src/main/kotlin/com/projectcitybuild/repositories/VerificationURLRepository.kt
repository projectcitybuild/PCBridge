package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import java.util.UUID

class VerificationURLRepository(
    private val accountLinkHTTPService: AccountLinkHTTPService,
) {
    suspend fun generateVerificationURL(playerUUID: UUID): String? {
        return accountLinkHTTPService.generateVerificationURL(playerUUID)
    }
}