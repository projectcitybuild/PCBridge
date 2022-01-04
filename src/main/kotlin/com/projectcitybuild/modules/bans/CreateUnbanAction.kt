package com.projectcitybuild.modules.bans

import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.Success
import com.projectcitybuild.entities.Result
import com.projectcitybuild.entities.models.ApiError
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIResult
import java.util.*

class CreateUnbanAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class HTTPError(error: ApiError?): FailReason()
        object NetworkError: FailReason()
        object PlayerNotBanned: FailReason()
    }

    suspend fun execute(playerId: UUID, staffId: UUID?): Result<Unit, FailReason> {
        val banApi = apiRequestFactory.pcb.banApi
        val response = apiClient.execute {
            banApi.storeUnban(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid"
            )
        }
        return when(response) {
            is APIResult.HTTPError -> {
                if (response.error?.id == "player_not_banned") {
                    return Failure(FailReason.PlayerNotBanned)
                }
                return Failure(FailReason.HTTPError(response.error))
            }
            is APIResult.NetworkError -> Failure(FailReason.NetworkError)
            is APIResult.Success -> Success(Unit)
        }
    }
}
