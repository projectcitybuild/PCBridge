package com.projectcitybuild.spigot.modules.bans.actions

import com.projectcitybuild.core.contracts.Environment

class CreateBanAction(private val environment: Environment) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_ALREADY_BANNED,
        DESERIALIZE_FAILED,
    }

    fun execute(playerName: String, staffId: String?, reason: String?) : Result {
        val banApi = environment.apiClient.banApi

        val request = banApi.storeBan(
                playerId = "bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f",
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId,
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
