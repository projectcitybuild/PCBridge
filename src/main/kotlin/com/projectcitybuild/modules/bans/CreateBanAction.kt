package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import java.util.*

class CreateBanAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class HTTPError(error: ApiError?): FailReason()
        object NetworkError: FailReason()
        object PlayerAlreadyBanned: FailReason()
    }

    suspend fun execute(playerId: UUID, playerName: String, staffId: UUID?, reason: String?): Result<Unit, FailReason> {
        val banApi = apiRequestFactory.pcb.banApi
        val response = apiClient.execute {
            banApi.storeBan(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid",
                reason = if (reason != null && reason.isNotEmpty()) reason else null,
                expiresAt = null,
                isGlobalBan = 1
            )
        }
        return when(response) {
            is APIResult.HTTPError -> {
                if (response.error?.id == "player_already_banned") {
                    return Failure(FailReason.PlayerAlreadyBanned)
                }
                return Failure(FailReason.HTTPError(response.error))
            }
            is APIResult.NetworkError -> Failure(FailReason.NetworkError)
            is APIResult.Success -> Success(Unit)
        }
    }
}
