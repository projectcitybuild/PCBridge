package com.projectcitybuild.actions

import com.projectcitybuild.api.APIProvider
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.models.MojangPlayer

class GetMojangPlayerAction(
        private val environment: EnvironmentProvider,
        private val apiProvider: APIProvider
) {
    sealed class Result {
        class SUCCESS(val player: MojangPlayer) : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        DESERIALIZE_FAILED,
    }

    fun execute(playerName: String, at: Long? = null) : Result {
        val mojangApi = apiProvider.mojang.mojangApi

        val request = mojangApi.getMojangPlayer(playerName, timestamp = at)
        val response = request.execute()
        val player = response.body() ?: return Result.FAILED(reason = Failure.DESERIALIZE_FAILED)

        return Result.SUCCESS(player)
    }
}
