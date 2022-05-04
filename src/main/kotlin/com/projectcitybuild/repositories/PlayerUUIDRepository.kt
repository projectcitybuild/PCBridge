package com.projectcitybuild.repositories

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.core.infrastructure.network.APIClient
import com.projectcitybuild.core.infrastructure.network.APIRequestFactory
import com.projectcitybuild.entities.responses.MojangPlayer
import org.bukkit.Server
import java.util.UUID
import javax.inject.Inject

open class PlayerUUIDRepository @Inject constructor(
    private val server: Server,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
) {
    class PlayerNotFoundException : Exception()

    // TODO: cache with expiry time
    private val mojangPlayerCache = HashMap<String, MojangPlayer>()

    suspend fun get(playerName: String): UUID? {
        val onlinePlayerUUID = server.getPlayer(playerName)?.uniqueId
        if (onlinePlayerUUID != null) {
            return onlinePlayerUUID
        }
        return try {
            val mojangPlayer = getMojangPlayer(playerName = playerName)
            UUID.fromString(mojangPlayer.uuid.toDashFormattedUUID())
        } catch (e: PlayerNotFoundException) {
            null
        }
    }

    private suspend fun getMojangPlayer(playerName: String, at: Long? = null): MojangPlayer {
        val cacheHit = mojangPlayerCache[playerName]
        if (cacheHit != null) {
            return cacheHit
        }

        val mojangApi = apiRequestFactory.mojang.mojangApi

        return apiClient.execute {
            try {
                val player = mojangApi.getMojangPlayer(playerName, timestamp = at)
                    ?: throw PlayerNotFoundException()

                mojangPlayerCache[playerName] = player
                player
            } catch (e: KotlinNullPointerException) {
                // Hacky workaround to catch 204 HTTP errors (username not found)
                throw PlayerNotFoundException()
            }
        }
    }
}
