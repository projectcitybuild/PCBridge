package com.projectcitybuild.features.bans.repositories

import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.entities.responses.GameBan
import java.util.*

class BanRepository(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient
) {
    class PlayerAlreadyBannedException : Exception()
    class PlayerNotBannedException : Exception()

    suspend fun ban(
        targetPlayerUUID: UUID,
        targetPlayerName: String,
        staffId: UUID?,
        reason: String?
    ) {
        try {
            val banApi = apiRequestFactory.pcb.banApi
            apiClient.execute {
                banApi.storeBan(
                    playerId = targetPlayerUUID.toString(),
                    playerIdType = "minecraft_uuid",
                    playerAlias = targetPlayerName,
                    staffId = staffId.toString(),
                    staffIdType = "minecraft_uuid",
                    reason = if (reason != null && reason.isNotEmpty()) reason else null,
                    expiresAt = null,
                    isGlobalBan = 1
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "player_already_banned") {
                throw PlayerAlreadyBannedException()
            }
            throw e
        }
    }

    suspend fun unban(targetPlayerUUID: UUID, staffId: UUID?) {
        try {
            val banApi = apiRequestFactory.pcb.banApi
            apiClient.execute {
                banApi.storeUnban(
                    playerId = targetPlayerUUID.toString(),
                    playerIdType = "minecraft_uuid",
                    staffId = staffId.toString(),
                    staffIdType = "minecraft_uuid"
                )
            }
        } catch (e: APIClient.HTTPError) {
            if (e.errorBody?.id == "player_not_banned") {
                throw PlayerNotBannedException()
            }
            throw e
        }
    }

    suspend fun get(targetPlayerUUID: UUID) : GameBan? {
        val banApi = apiRequestFactory.pcb.banApi
        val response = apiClient.execute {
            banApi.requestStatus(
                playerId = targetPlayerUUID.toString(),
                playerType = "minecraft_uuid"
            )
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