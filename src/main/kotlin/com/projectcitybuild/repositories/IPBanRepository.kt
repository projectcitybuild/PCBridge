package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.entities.responses.IPBan
import java.util.UUID

class IPBanRepository(
    private val apiClient: APIClient,
    private val apiRequestFactory: APIRequestFactory,
) {
    class IPAlreadyBannedException : Exception()
    class IPNotBannedException : Exception()

    suspend fun get(ip: String): IPBan? {
        return apiClient.execute {
            apiRequestFactory.pcb.ipBanAPI.status(ip = ip)
        }.data
    }

    @Throws(IPAlreadyBannedException::class)
    suspend fun ban(
        ip: String,
        bannerUUID: UUID,
        bannerName: String,
        reason: String,
    ) {
        try {
            apiClient.execute {
                apiRequestFactory.pcb.ipBanAPI.ban(
                    ip = ip,
                    bannerPlayerId = bannerUUID.toString(),
                    bannerPlayerAlias = bannerName,
                    reason = reason,
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "ip_already_banned") {
                throw IPAlreadyBannedException()
            }
            throw e
        }
    }

    @Throws(IPNotBannedException::class)
    suspend fun unban(
        ip: String,
        unbannerUUID: UUID,
        unbannerName: String,
    ) {
        try {
            apiClient.execute {
                apiRequestFactory.pcb.ipBanAPI.unban(
                    ip = ip,
                    unbannerPlayerId = unbannerUUID.toString(),
                    unbannerPlayerAlias = unbannerName,
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "ip_not_banned") {
                throw IPNotBannedException()
            }
            throw e
        }
    }
}
