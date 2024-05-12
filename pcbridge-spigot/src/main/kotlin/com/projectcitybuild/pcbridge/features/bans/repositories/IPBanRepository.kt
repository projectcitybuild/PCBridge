package com.projectcitybuild.pcbridge.features.bans.repositories

import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import java.util.UUID

class IPBanRepository(
    private val httpService: IPBanHttpService,
) {
    suspend fun get(ip: String): IPBan? {
        return httpService.get(ip)
    }

    @Throws(IPBanHttpService.IPAlreadyBannedException::class)
    suspend fun ban(
        ip: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String,
    ) {
        httpService.ban(
            ip = ip,
            bannerUUID = bannerUUID,
            bannerName = bannerName,
            reason = reason,
        )
    }

    @Throws(IPBanHttpService.IPNotBannedException::class)
    suspend fun unban(
        ip: String,
        unbannerUUID: UUID?,
        unbannerName: String,
    ) {
        httpService.unban(
            ip = ip,
            unbannerUUID = unbannerUUID,
            unbannerName = unbannerName,
        )
    }
}
