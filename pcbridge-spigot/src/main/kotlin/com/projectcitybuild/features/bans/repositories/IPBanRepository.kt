package com.projectcitybuild.features.bans.repositories

import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import java.util.UUID

class IPBanRepository(
    private val ipBanHttpService: IPBanHttpService
) {
    suspend fun get(ip: String): IPBan? {
        return ipBanHttpService.get(ip)
    }

    @Throws(IPBanHttpService.IPAlreadyBannedException::class)
    suspend fun ban(
        ip: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String,
    ) {
        ipBanHttpService.ban(
            ip = ip,
            bannerUUID = bannerUUID,
            bannerName = bannerName,
            reason = reason
        )
    }

    @Throws(IPBanHttpService.IPNotBannedException::class)
    suspend fun unban(
        ip: String,
        unbannerUUID: UUID?,
        unbannerName: String,
    ) {
        ipBanHttpService.unban(
            ip = ip,
            unbannerUUID = unbannerUUID,
            unbannerName = unbannerName,
        )
    }
}
