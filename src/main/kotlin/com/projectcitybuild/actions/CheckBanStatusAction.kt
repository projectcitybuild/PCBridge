package com.projectcitybuild.actions

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.entities.models.GameBan
import java.util.*

class CheckBanStatusAction(private val environment: Environment) {
    sealed class Result {
        class SUCCESS(val ban: GameBan?) : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        DESERIALIZE_FAILED,
    }

    fun execute(playerId: UUID) : Result {
        val banApi = environment.apiClient.banApi

        val request = banApi.requestStatus(
                playerId = playerId.toString(),
                playerType = "minecraft_uuid"
        )
        val response = request.execute()
        val json = response.body()

        if (json == null) {
            return CheckBanStatusAction.Result.FAILED(reason = CheckBanStatusAction.Failure.DESERIALIZE_FAILED)
        }

        val ban = json.data
        if (ban == null) {
            return CheckBanStatusAction.Result.SUCCESS(null)
        }
        if (!ban.isActive) {
            return CheckBanStatusAction.Result.SUCCESS(null)
        }
        if (ban.expiresAt != null && ban.expiresAt <= Date().time) {
            return CheckBanStatusAction.Result.SUCCESS(null)
        }

        return CheckBanStatusAction.Result.SUCCESS(ban)
    }
}
