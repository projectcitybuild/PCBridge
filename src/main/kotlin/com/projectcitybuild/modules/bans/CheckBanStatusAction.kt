package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIResult
import java.util.*

class CheckBanStatusAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class HTTPError(error: ApiError?): FailReason()
        object NetworkError : FailReason()
    }

    suspend fun execute(playerId: UUID) : Result<GameBan?, FailReason> {
        val banApi = apiRequestFactory.pcb.banApi
        val response = apiClient.execute {
            banApi.requestStatus(
                playerId = playerId.toString(),
                playerType = "minecraft_uuid"
            )
        }
        return when (response) {
            is APIResult.HTTPError -> Failure(FailReason.HTTPError(response.error))
            is APIResult.NetworkError -> Failure(FailReason.NetworkError)
            is APIResult.Success -> {
                val ban = response.value.data
                if (ban == null) {
                    return Success(null)
                }
                if (!ban.isActive) {
                    return Success(null)
                }
                if (ban.expiresAt != null && ban.expiresAt <= Date().time) {
                    return Success(null)
                }
                return Success(ban)
            }
        }
    }
}
