package com.projectcitybuild.actions

import com.projectcitybuild.core.contracts.EnvironmentProvider
import java.util.*

class CreateBanAction(private val environment: EnvironmentProvider) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_ALREADY_BANNED,
        DESERIALIZE_FAILED,
    }

    fun execute(playerId: UUID, playerName: String, staffId: UUID?, reason: String?) : Result {
        val banApi = environment.apiClient.banApi

        val request = banApi.storeBan(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid",
                reason = reason,
                expiresAt = null,
                isGlobalBan = true
        )
        val response = request.execute()
        val json = response.body()

        if (json?.error != null && json.error.id == "player_already_banned") {
            return Result.FAILED(reason = Failure.PLAYER_ALREADY_BANNED)
        }
        if (json == null || json.data == null) {
            return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
        }

        return Result.SUCCESS()
    }
}
