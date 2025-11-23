package com.projectcitybuild.pcbridge.paper.features.bans.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import com.projectcitybuild.pcbridge.http.pcb.services.UuidBanHttpService
import java.util.UUID

class UuidBanRepository(
    private val uuidBanHttpService: UuidBanHttpService,
) {
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