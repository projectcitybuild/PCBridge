package com.projectcitybuild.repositories

import com.projectcitybuild.pcbridge.http.clients.PCBClient
import com.projectcitybuild.pcbridge.http.core.APIClient
import com.projectcitybuild.pcbridge.http.responses.PlayerBan
import java.util.UUID

class PlayerBanRepository(
    private val pcbClient: PCBClient,
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
        reason: String?,
        expiryDate: Long? = null,
    ) {
        try {
            apiClient.execute {
                pcbClient.playerBanAPI.ban(
                    bannedPlayerId = targetPlayerUUID.toString(),
                    bannedPlayerAlias = targetPlayerName,
                    bannerPlayerId = bannerPlayerUUID.toString(),
                    bannerPlayerAlias = bannerPlayerName,
                    reason = reason,
                    expiresAt = expiryDate,
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
            apiClient.execute {
                pcbClient.playerBanAPI.unban(
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

    suspend fun get(targetPlayerUUID: UUID): PlayerBan? {
        val response = apiClient.execute {
            pcbClient.playerBanAPI.status(
                playerId = targetPlayerUUID.toString(),
            )
        }
        val ban = response.data
        if (ban?.unbannedAt != null) {
            return null
        }
        return ban
    }
}
