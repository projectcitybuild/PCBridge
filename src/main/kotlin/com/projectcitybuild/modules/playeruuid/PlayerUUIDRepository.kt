package com.projectcitybuild.modules.playeruuid

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import net.md_5.bungee.api.ProxyServer
import java.util.*

class PlayerUUIDRepository(
    private val proxyServer: ProxyServer,
    private val mojangPlayerRepository: MojangPlayerRepository
) {
    suspend fun request(playerName: String): UUID? {
        val onlinePlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == playerName.lowercase() }

        if (onlinePlayer != null) {
            return onlinePlayer.uniqueId
        }
        return try {
            val mojangPlayer = mojangPlayerRepository.get(playerName = playerName)
            UUID.fromString(mojangPlayer.uuid.toDashFormattedUUID())
        } catch (e: MojangPlayerRepository.PlayerNotFoundException) {
            null
        }
    }
}