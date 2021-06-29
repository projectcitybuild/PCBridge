package com.projectcitybuild.modules.bans

import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.contracts.EnvironmentProvider
import java.util.*

class CreateBanAction(
        private val networkClients: NetworkClients
) {
    sealed class Result {
        class SUCCESS : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        PLAYER_ALREADY_BANNED,
        DESERIALIZE_FAILED,
        BAD_REQUEST,
        UNHANDLED,
    }

    fun execute(playerId: UUID, playerName: String, staffId: UUID?, reason: String?) : Result {
        val banApi = networkClients.pcb.banApi

        val request = banApi.storeBan(
                playerId = playerId.toString(),
                playerIdType = "minecraft_uuid",
                playerAlias = playerName,
                staffId = staffId.toString(),
                staffIdType = "minecraft_uuid",
                reason = reason,
                expiresAt = null,
                isGlobalBan = 1
        )
        val response = request.execute()
        val json = response.body()

        if (json?.error != null) {
            return when (json.error.id) {
                "player_already_banned" -> Result.FAILED(reason = Failure.PLAYER_ALREADY_BANNED)
                "bad_input" -> Result.FAILED(reason = Failure.BAD_REQUEST)
                else -> Result.FAILED(reason = Failure.UNHANDLED)
            }
        }
        if (json?.data == null) {
            return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)
        }

        return Result.SUCCESS()
    }
}
