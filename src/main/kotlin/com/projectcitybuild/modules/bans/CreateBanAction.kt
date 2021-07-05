package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import java.util.*

class CreateBanAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class PLAYER_ALREADY_BANNED: FailReason()
        class API_ERROR(val message: String): FailReason()
        class UNHANDLED: FailReason()
    }

    fun execute(
            playerId: UUID,
            playerName: String,
            staffId: UUID?,
            reason: String?,
            completion: (Result<Unit, FailReason>) -> Unit
    ) {
        val banApi = apiRequestFactory.pcb.banApi
        val request = banApi.storeBan(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid",
                reason = if (reason.isNullOrEmpty()) null else reason,
                expiresAt = null,
                isGlobalBan = 1
        )
        apiClient.execute(request).startAndSubscribe { result ->
            when (result) {
                is Success -> completion(Success(Unit))

                is Failure -> {
                    val responseBody = result.reason.responseBody
                    if (responseBody != null) {
                        val reason = when (responseBody.id) {
                            "player_already_banned" -> FailReason.PLAYER_ALREADY_BANNED()
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
