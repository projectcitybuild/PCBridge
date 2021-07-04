package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.entities.APIClientError
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import java.util.*

class CreateBanAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_ALREADY_BANNED,
        UNHANDLED,
    }

    fun execute(playerId: UUID, playerName: String, staffId: UUID?, reason: String?) : Result {
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
                is APIClient.Result.FAILURE -> {
                    when (result.error) {
                        is APIClientError.BODY -> {
                            when (result.error.error.id) {
                                "player_already_banned" -> Result.FAILED(reason = Failure.PLAYER_ALREADY_BANNED)
                                "bad_input" -> Result.FAILED(reason = Failure.BAD_REQUEST)
                                else -> Result.FAILED(reason = Failure.UNHANDLED)
                            }
                        }
                    }
                }
            }
        }
    }
}
