package com.projectcitybuild.repositories

import com.projectcitybuild.extensions.toDashFormattedUUID
import com.projectcitybuild.pcbridge.http.clients.MojangClient
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.responses.MojangPlayer
import org.bukkit.Server
import java.util.UUID

open class PlayerUUIDRepository(
    private val server: Server,
    private val mojangClient: MojangClient,
    private val apiClient: ResponseParser,
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

        val mojangApi = mojangClient.mojangApi

        return apiClient.parse {
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
