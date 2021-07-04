package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.models.GameBan
import java.util.*

class CheckBanStatusAction(
        private val apiRequestFactory: APIRequestFactory
) {
    sealed class Result {
        class SUCCESS(val ban: GameBan?) : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        DESERIALIZE_FAILED,
    }

    fun execute(playerId: UUID) : Result {
        val banApi = apiRequestFactory.pcb.banApi

        val request = banApi.requestStatus(
                playerId = playerId.toString(),
                playerType = "minecraft_uuid"
        )
        val response = request.execute()
        val json = response.body()

        if (json == null) {
            return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
        }

        val ban = json.data
        if (ban == null) {
            return Result.SUCCESS(null)
        }
        if (!ban.isActive) {
            return Result.SUCCESS(null)
        }
        if (ban.expiresAt != null && ban.expiresAt <= Date().time) {
            return Result.SUCCESS(null)
        }

        return Result.SUCCESS(ban)
    }
}
