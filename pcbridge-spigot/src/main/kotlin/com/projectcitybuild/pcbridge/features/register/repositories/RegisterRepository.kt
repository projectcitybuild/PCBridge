package com.projectcitybuild.pcbridge.features.register.repositories

import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import java.util.UUID

class RegisterRepository(
    private val registerHttpService: RegisterHttpService,
) {
    suspend fun sendCode(
        email: String,
        playerAlias: String,
        playerUUID: UUID,
    ) {
        registerHttpService.sendCode(
            email = email,
            playerAlias = playerAlias,
            playerUUID = playerUUID,
        )
    }
}
