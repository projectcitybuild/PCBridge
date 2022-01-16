package com.projectcitybuild.modules.playeruuid

import com.projectcitybuild.entities.responses.MojangPlayer
import com.projectcitybuild.modules.network.APIClient
import com.projectcitybuild.modules.network.APIRequestFactory
import javax.inject.Inject

class MojangPlayerRepository @Inject constructor(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    class PlayerNotFoundException: Exception()

    // TODO: cache with expiry time
    private val cache = HashMap<String, MojangPlayer>()

    suspend fun get(playerName: String, at: Long? = null): MojangPlayer {
        val cacheHit = cache[playerName]
        if (cacheHit != null) {
            return cacheHit
        }

        val mojangApi = apiRequestFactory.mojang.mojangApi

        return apiClient.execute {
            try {
                val player = mojangApi.getMojangPlayer(playerName, timestamp = at)
                if (player == null) {
                    throw PlayerNotFoundException()
                }
                cache[playerName] = player
                player

            } catch (e: KotlinNullPointerException) {
                // Hacky workaround to catch 204 HTTP errors (username not found)
                throw PlayerNotFoundException()
            }
        }
    }
}