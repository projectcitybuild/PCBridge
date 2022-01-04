package com.projectcitybuild.modules.players

import com.projectcitybuild.entities.Success
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
            val result = getMojangPlayerAction.execute(playerName = playerName)
            if (result is Success) {
                return UUID.fromString(result.value.uuid.toDashFormattedUUID())
            }
        }
        return onlinePlayer?.uniqueId
    }
}