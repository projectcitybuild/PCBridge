package com.projectcitybuild.spigot.extensions

import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.spigot.modules.bans.actions.GetMojangPlayerAction
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.*

fun Server.getOfflinePlayer(name: String, environment : Environment) : UUID? {
    val player = this.getOnlinePlayer(name)
    if (player != null) {
        return player.uniqueId
    }
    val mojangPlayerAction = GetMojangPlayerAction(environment)
    val result = mojangPlayerAction.execute(playerName = name)
    if (result is GetMojangPlayerAction.Result.SUCCESS) {
        return UUID.fromString(result.player.uuid.toDashFormattedUUID())
    }
    return null
}

fun Server.getOnlinePlayer(name: String) : Player? {
    return onlinePlayers?.find { player -> player.name.toLowerCase() == name.toLowerCase() }
}