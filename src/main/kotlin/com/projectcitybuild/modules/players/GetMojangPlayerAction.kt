package com.projectcitybuild.modules.players

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.models.MojangPlayer

class GetMojangPlayerAction(
        private val apiRequestFactory: APIRequestFactory
) {
    sealed class Result {
        class SUCCESS(val player: MojangPlayer) : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        DESERIALIZE_FAILED,
    }

    fun execute(playerName: String, at: Long? = null) : Result {
        val mojangApi = apiRequestFactory.mojang.mojangApi

        val request = mojangApi.getMojangPlayer(playerName, timestamp = at)
        val response = request.execute()
        val player = response.body() ?: return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)

        return Result.SUCCESS(player)
    }
}
