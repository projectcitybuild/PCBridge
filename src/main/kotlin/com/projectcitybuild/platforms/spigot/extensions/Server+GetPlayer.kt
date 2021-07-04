package com.projectcitybuild.platforms.spigot.extensions

import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.core.network.NetworkClients
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.*

//@Deprecated("Use an action instead")
//fun Server.getOfflinePlayer(
//        name: String,
//        networkClients: NetworkClients
//) : UUID? {
//    val player = this.getOnlinePlayer(name)
//    if (player != null) {
//        return player.uniqueId
//    }
//    val mojangPlayerAction = GetMojangPlayerAction(networkClients)
//    val result = mojangPlayerAction.execute(playerName = name)
//    if (result is GetMojangPlayerAction.Result.SUCCESS) {
//        return UUID.fromString(result.player.uuid.toDashFormattedUUID())
//    }
//    if (result is GetMojangPlayerAction.Result.FAILED) {
//        when (result.reason) {
//            GetMojangPlayerAction.Failure.DESERIALIZE_FAILED -> throw Exception("Bad response from Mojang server when fetching UUID for offline player")
//        }
//    }
//    return null
//}
//
//fun Server.getOnlinePlayer(name: String) : Player? {
//    return onlinePlayers?.find { player -> player.name.toLowerCase() == name.toLowerCase() }
//}