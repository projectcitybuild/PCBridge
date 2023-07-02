package com.projectcitybuild.repositories

import com.projectcitybuild.extensions.toDashFormattedUUID
import com.projectcitybuild.pcbridge.http.responses.MojangPlayer
import com.projectcitybuild.pcbridge.http.services.mojang.PlayerUUIDHttpService
import org.bukkit.Server
import java.util.UUID

open class PlayerUUIDRepository(
    private val server: Server,
    private val playerUUIDHttpService: PlayerUUIDHttpService,
) {
    // TODO: cache with expiry time
    private val mojangPlayerCache = HashMap<String, MojangPlayer>()

    suspend fun get(playerName: String): UUID? {
        val onlinePlayerUUID = server.getPlayer(playerName)?.uniqueId
        if (onlinePlayerUUID != null) {
            return onlinePlayerUUID
        }
        return fetchFromMojang(playerName = playerName)?.let {
            UUID.fromString(it.uuid.toDashFormattedUUID())
        }
    }

    private suspend fun fetchFromMojang(playerName: String): MojangPlayer? {
        val cacheHit = mojangPlayerCache[playerName]
        if (cacheHit != null) {
            return cacheHit
        }
        return try {
            playerUUIDHttpService.get(playerName)
        } catch (e: PlayerUUIDHttpService.PlayerNotFoundException) {
            null
        }
    }
}
