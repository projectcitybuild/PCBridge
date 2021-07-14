package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.models.GameBan
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.network.APIClient
import java.util.*

class CheckBanStatusAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class API_ERROR(val message: String): FailReason()
    }

    fun execute(
        playerId: UUID,
        completion: (Result<GameBan?, FailReason>) -> Unit
    ) {
        val banApi = apiRequestFactory.pcb.banApi
        val request = banApi.requestStatus(
                playerId = playerId.toString(),
                playerType = "minecraft_uuid"
        )
        apiClient.execute(request).startAndSubscribe { result ->
            when (result) {
                is Success -> {
                    val ban = result.value
                    if (!ban.isActive) {
                        completion(Success(null))
                        return@startAndSubscribe
                    }
                    if (ban.expiresAt != null && ban.expiresAt <= Date().time) {
                        completion(Success(null))
                        return@startAndSubscribe
                    }
                    completion(Success(ban))
                }
                is Failure -> completion(Failure(FailReason.API_ERROR(message = result.reason.message)))
            }
        }
    }
}
