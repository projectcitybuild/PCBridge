package com.projectcitybuild.pcbridge.features.bans.domain.repositories

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.UuidBanHttpService
import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParserError
import java.util.UUID
import kotlin.jvm.Throws

class UuidBanRepository(
    private val uuidBanHttpService: UuidBanHttpService,
) {
    @Throws(ResponseParserError::class)
    suspend fun create(
        bannedUUID: UUID,
        bannedAlias: String,
        bannerUUID: UUID? = null,
        bannerAlias: String? = null,
        reason: String,
        additionalInfo: String?,
    ): PlayerBan {
        return uuidBanHttpService.create(
            bannedUUID = bannedUUID,
            bannedAlias = bannedAlias,
            bannerUUID = bannerUUID,
            bannerAlias = bannerAlias,
            reason = reason,
            additionalInfo = additionalInfo,
        )
    }
}
