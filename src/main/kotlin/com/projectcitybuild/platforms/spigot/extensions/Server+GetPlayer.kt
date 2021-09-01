package com.projectcitybuild.platforms.spigot.extensions

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.core.network.APIRequestFactory
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.*

// TODO: move this into new Action
suspend fun Server.getOfflinePlayer(
    name: String,
    apiRequestFactory: APIRequestFactory,
    apiClient: APIClient
) : UUID? {
    val player = this.getOnlinePlayer(name)
    if (player != null) {
        return player.uniqueId
    }
    val result = GetMojangPlayerAction(apiRequestFactory, apiClient).execute(playerName = name)
    return when (result) {
        is Success -> UUID.fromString(result.value.uuid.toDashFormattedUUID())
        else -> return null
    }
}

fun Server.getOnlinePlayer(name: String) : Player? {
    return onlinePlayers?.find { player -> player.name.toLowerCase() == name.toLowerCase() }
}