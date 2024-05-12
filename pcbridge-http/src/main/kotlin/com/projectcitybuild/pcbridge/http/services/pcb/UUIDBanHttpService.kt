package com.projectcitybuild.pcbridge.http.services.pcb

import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.util.UUID

class UUIDBanHttpService(
    private val retrofit: Retrofit,
    private val responseParser: ResponseParser,
) {
    class UUIDAlreadyBannedException : Exception()

    class UUIDNotBannedException : Exception()

    suspend fun get(targetPlayerUUID: UUID): PlayerBan? =
        withContext(Dispatchers.IO) {
            val response =
                responseParser.parse {
                    retrofit.pcb().getUuidBanStatus(
                        playerId = targetPlayerUUID.toString(),
                    )
                }
            val ban = response.data
            if (ban?.unbannedAt != null) {
                null
            }
            ban
        }

    @Throws(UUIDAlreadyBannedException::class)
    suspend fun ban(
        targetPlayerUUID: UUID,
        targetPlayerName: String,
        bannerPlayerUUID: UUID?,
        bannerPlayerName: String,
        reason: String?,
        expiryDate: Long? = null,
    ) = withContext(Dispatchers.IO) {
        try {
            responseParser.parse {
                retrofit.pcb().banUUID(
                    bannedPlayerId = targetPlayerUUID.toString(),
                    bannedPlayerAlias = targetPlayerName,
                    bannerPlayerId = bannerPlayerUUID.toString(),
                    bannerPlayerAlias = bannerPlayerName,
                    reason = reason,
                    expiresAt = expiryDate,
                )
            }
        } catch (e: ResponseParser.HTTPError) {
            if (e.errorBody?.id == "player_already_banned") {
                throw UUIDAlreadyBannedException()
            }
            throw e
        }
    }

    @Throws(UUIDNotBannedException::class)
    suspend fun unban(
        targetPlayerUUID: UUID,
        unbannerUUID: UUID?,
    ) = withContext(Dispatchers.IO) {
        try {
            responseParser.parse {
                retrofit.pcb().unbanUUID(
                    bannedPlayerId = targetPlayerUUID.toString(),
                    unbannerPlayerId = unbannerUUID.toString(),
                )
            }
        } catch (e: ResponseParser.HTTPError) {
            if (e.errorBody?.id == "player_not_banned") {
                throw UUIDNotBannedException()
            }
            throw e
        }
    }
}
