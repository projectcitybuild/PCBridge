package com.projectcitybuild.modules.players

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import net.md_5.bungee.api.ProxyServer
import java.util.*

class PlayerUUIDLookup(
    private val proxyServer: ProxyServer,
    private val getMojangPlayerAction: GetMojangPlayerAction
) {
    suspend fun request(playerName: String): UUID? {
        val onlinePlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == playerName.lowercase() }

        if (onlinePlayer == null) {
            val mojangPlayer = getMojangPlayerAction.execute(playerName = playerName)
            return UUID.fromString(mojangPlayer.uuid.toDashFormattedUUID())
        }
        return onlinePlayer.uniqueId
    }
}