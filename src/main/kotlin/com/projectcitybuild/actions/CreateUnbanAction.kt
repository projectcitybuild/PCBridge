package com.projectcitybuild.actions

import com.projectcitybuild.core.contracts.EnvironmentProvider
import java.util.*

class CreateUnbanAction(private val environment: EnvironmentProvider) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_NOT_BANNED,
        DESERIALIZE_FAILED,
    }

    fun execute(playerId: UUID, staffId: UUID?) : Result {
        val banApi = environment.apiClient.banApi

        val request = banApi.storeUnban(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid"
        )
        val response = request.execute()
        val json = response.body()

        if (json?.error != null && json.error.id == "player_not_banned") {
            return Result.FAILED(reason = Failure.PLAYER_NOT_BANNED)
        }
        if (json?.data == null) {
            return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
        }

        return Result.SUCCESS()
    }
}
