package com.projectcitybuild.modules.playeruuid

import com.projectcitybuild.modules.network.APIRequestFactory
import com.projectcitybuild.entities.responses.MojangPlayer
import com.projectcitybuild.modules.network.APIClient

class MojangPlayerRepository(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    class PlayerNotFoundException: Exception()

    suspend fun get(playerName: String, at: Long? = null): MojangPlayer {
        val mojangApi = apiRequestFactory.mojang.mojangApi

        return apiClient.execute {
            try {
                mojangApi.getMojangPlayer(playerName, timestamp = at)
                    ?: throw PlayerNotFoundException()
            } catch (e: KotlinNullPointerException) {
                // Hacky workaround to catch 204 HTTP errors (username not found)
                throw PlayerNotFoundException()
            }
        }
    }
}