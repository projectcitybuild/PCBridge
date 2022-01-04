package com.projectcitybuild.modules.players

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.models.ApiError
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.models.MojangPlayer
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIResult

class GetMojangPlayerAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class HTTPError(error: ApiError?): FailReason()
        object NetworkError: FailReason()
        object PlayerNotFound: FailReason()
    }

    suspend fun execute(playerName: String, at: Long? = null): Result<MojangPlayer, FailReason> {
        val mojangApi = apiRequestFactory.mojang.mojangApi
        val response = apiClient.execute { mojangApi.getMojangPlayer(playerName, timestamp = at) }

        return when (response) {
            is APIResult.HTTPError -> Failure(FailReason.HTTPError(response.error))
            is APIResult.NetworkError -> Failure(FailReason.NetworkError)
            is APIResult.Success -> {
                if (response.value == null) Failure(FailReason.PlayerNotFound)
                else Success(response.value)
            }
        }
    }
}
