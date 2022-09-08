package com.projectcitybuild.repositories

import com.projectcitybuild.core.http.APIRequestFactory
import com.projectcitybuild.core.http.core.APIClient
import com.projectcitybuild.entities.responses.GameBan
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class BanRepository @Inject constructor(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    class PlayerAlreadyBannedException : Exception()
    class PlayerNotBannedException : Exception()

    @Throws(PlayerAlreadyBannedException::class)
    suspend fun ban(
        targetPlayerUUID: UUID,
        targetPlayerName: String,
        bannerPlayerUUID: UUID?,
        bannerPlayerName: String,
        reason: String?
    ) {
        try {
            val banApi = apiRequestFactory.pcb.banAPI
            apiClient.execute {
                banApi.ban(
                    bannedPlayerId = targetPlayerUUID.toString(),
                    bannedPlayerAlias = targetPlayerName,
                    bannerPlayerId = bannerPlayerUUID.toString(),
                    bannerPlayerAlias = bannerPlayerName,
                    reason = reason,
                    expiresAt = null,
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "player_already_banned") {
                throw PlayerAlreadyBannedException()
            }
            throw e
        }
    }

    @Throws(PlayerNotBannedException::class)
    suspend fun unban(targetPlayerUUID: UUID, staffId: UUID?) {
        try {
            val banApi = apiRequestFactory.pcb.banAPI
            apiClient.execute {
                banApi.unban(
                    bannedPlayerId = targetPlayerUUID.toString(),
                    unbannerPlayerId = staffId.toString(),
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "player_not_banned") {
                throw PlayerNotBannedException()
            }
            throw e
        }
    }

    suspend fun get(targetPlayerUUID: UUID): GameBan? {
        val banApi = apiRequestFactory.pcb.banAPI
        val response = apiClient.execute {
            banApi.status(playerId = targetPlayerUUID.toString())
        }
        val ban = response.data
        if (ban != null) {
            if (!ban.isActive) return null

            val hasExpired = ban.expiresAt != null && ban.expiresAt <= Date().time
            if (hasExpired) return null
        }
        return ban
    }
}
