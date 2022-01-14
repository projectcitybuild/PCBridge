package com.projectcitybuild.modules.playeruuid

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.modules.network.APIClient
import net.md_5.bungee.api.ProxyServer
import java.util.*

class PlayerUUIDRepository(
    private val proxyServer: ProxyServer,
    private val getMojangPlayerAction: MojangPlayerRepository
) {
    suspend fun request(playerName: String): UUID? {
        val onlinePlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == playerName.lowercase() }

        if (onlinePlayer != null) {
            return onlinePlayer.uniqueId
        }

        try {
            val mojangPlayer = getMojangPlayerAction.get(playerName = playerName)
            return UUID.fromString(mojangPlayer.uuid.toDashFormattedUUID())
        } catch (e: APIClient.HTTPError) {
            // "Username not found" is returned to us as a 204 HTTP error
            if (e.errorBody?.status == 204) {
                return null
            }
            throw e
        }
    }
}