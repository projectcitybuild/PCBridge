package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.IPBan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class IPBanHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class IPAlreadyBannedException : Exception()
    class IPNotBannedException : Exception()

    suspend fun get(ip: String): IPBan? {
        val response = responseParser.parse {
            retrofit.pcb().getIPStatus(ip = ip)
        }
        return response.data
    }

    @Throws(IPAlreadyBannedException::class)
    suspend fun ban(
        ip: String,
        bannerUUID: UUID?,
        bannerName: String,
        reason: String,
    ) = withContext(Dispatchers.IO) {
        try {
            responseParser.parse {
                retrofit.pcb().banIP(
                    ip = ip,
                    bannerPlayerId = bannerUUID.toString(),
                    bannerPlayerAlias = bannerName,
                    reason = reason,
                )
            }
        } catch (e: ResponseParser.HTTPError) {
            if (e.errorBody?.id == "ip_already_banned") {
                throw IPAlreadyBannedException()
            }
            throw e
        }
    }

    @Throws(IPNotBannedException::class)
    suspend fun unban(
        ip: String,
        unbannerUUID: UUID?,
        unbannerName: String,
    ) = withContext(Dispatchers.IO) {
        try {
            responseParser.parse {
                retrofit.pcb().unbanIP(
                    ip = ip,
                    unbannerPlayerId = unbannerUUID.toString(),
                    unbannerPlayerAlias = unbannerName,
                )
            }
        } catch (e: ResponseParser.HTTPError) {
            if (e.errorBody?.id == "ip_not_banned") {
                throw IPNotBannedException()
            }
            throw e
        }
    }
}