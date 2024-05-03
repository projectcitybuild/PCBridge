package com.projectcitybuild.features.bans.repositories

import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import java.util.UUID

class PlayerBanRepository(
    private val uuidBanHttpService: UUIDBanHttpService,
) {
    suspend fun get(targetPlayerUUID: UUID): PlayerBan? {
        return uuidBanHttpService.get(targetPlayerUUID)
    }

    @Throws(UUIDBanHttpService.UUIDAlreadyBannedException::class)
    suspend fun ban(
        targetPlayerUUID: UUID,
        targetPlayerName: String,
        bannerPlayerUUID: UUID?,
        bannerPlayerName: String,
        reason: String?,
        expiryDate: Long? = null,
    ) {
        uuidBanHttpService.ban(
            targetPlayerUUID = targetPlayerUUID,
            targetPlayerName = targetPlayerName,
            bannerPlayerUUID = bannerPlayerUUID,
            bannerPlayerName = bannerPlayerName,
            reason = reason,
            expiryDate = expiryDate,
        )
    }

    @Throws(UUIDBanHttpService.UUIDNotBannedException::class)
    suspend fun unban(
        targetPlayerUUID: UUID,
        unbannerUUID: UUID?,
    ) {
        uuidBanHttpService.unban(
            targetPlayerUUID = targetPlayerUUID,
            unbannerUUID = unbannerUUID,
        )
    }
}
