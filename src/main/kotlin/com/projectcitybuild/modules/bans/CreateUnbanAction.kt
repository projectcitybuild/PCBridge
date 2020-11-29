package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.api.APIProvider
import com.projectcitybuild.core.contracts.EnvironmentProvider
import java.util.*

class CreateUnbanAction(
        private val environment: EnvironmentProvider,
        private val apiProvider: APIProvider
) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_NOT_BANNED,
        DESERIALIZE_FAILED,
        BAD_REQUEST,
    }

    fun execute(playerId: UUID, staffId: UUID?) : Result {
        val banApi = apiProvider.pcb.banApi

        val request = banApi.storeUnban(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid"
        )
        val response = request.execute()
        val json = response.body()

        if (json?.error != null) {
            when (json.error.id) {
                "player_not_banned" -> return Result.FAILED(reason = Failure.PLAYER_NOT_BANNED)
                "bad_input" -> return Result.FAILED(reason = Failure.BAD_REQUEST)
            }
        }
        if (json?.data == null) {
            return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
        }

        return Result.SUCCESS()
    }
}
