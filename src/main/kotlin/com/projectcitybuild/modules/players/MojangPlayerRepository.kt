package com.projectcitybuild.modules.players

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.entities.responses.MojangPlayer
import com.projectcitybuild.core.network.APIClient

class MojangPlayerRepository(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    class PlayerNotFoundException: Exception()

    suspend fun get(playerName: String, at: Long? = null): MojangPlayer {
        val mojangApi = apiRequestFactory.mojang.mojangApi
        return apiClient.execute { mojangApi.getMojangPlayer(playerName, timestamp = at) }
            ?: throw PlayerNotFoundException()
    }
}