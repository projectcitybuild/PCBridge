package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import java.util.*

class CreateUnbanAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class PLAYER_NOT_BANNED : FailReason()
        class API_ERROR(val message: String) : FailReason()
        class UNHANDLED : FailReason()
    }

    fun execute(
        playerId: UUID,
        staffId: UUID?,
        completion: (Result<Unit, FailReason>) -> Unit
    ) {
        val banApi = apiRequestFactory.pcb.banApi
        val request = banApi.storeUnban(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid"
        )
        apiClient.execute(request).startAndSubscribe { result ->
            when (result) {
                is Success -> completion(Success(Unit))

                is Failure -> {
                    val responseBody = result.reason.responseBody
                    if (responseBody != null) {
                        val reason = when (responseBody.id) {
                            "player_not_banned" -> FailReason.PLAYER_NOT_BANNED()
                            else -> FailReason.UNHANDLED()
                        }
                        completion(Failure(reason))
                    } else {
                        completion(Failure(FailReason.API_ERROR(result.reason.message)))
                    }
                }
            }
        }
    }
}
