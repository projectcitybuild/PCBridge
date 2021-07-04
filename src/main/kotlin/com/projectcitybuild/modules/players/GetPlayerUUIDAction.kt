package com.projectcitybuild.modules.players

import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.core.network.NetworkClients
import net.md_5.bungee.api.ProxyServer
import java.util.*

class GetPlayerUUIDAction(private val mojangPlayerAction: GetMojangPlayerAction) {

    sealed class Result {
        class SUCCESS(val uuid: UUID) : Result()
        class FAILED(val reason: Failure) : Result()
    }

    enum class Failure {
        FETCH_FAILED
    }

    fun execute(playerName: String, proxyServer: ProxyServer): Result {
        // Search for online player first
        val onlinePlayer = proxyServer.getPlayer(playerName)
        if (onlinePlayer != null) {
            return Result.SUCCESS(onlinePlayer.uniqueId)
        }

        // Otherwise fetch from Mojang's API
        val result = mojangPlayerAction.execute(playerName)
        return when (result) {
            is GetMojangPlayerAction.Result.SUCCESS -> {
                val uuid = UUID.fromString(result.player.uuid.toDashFormattedUUID())
                Result.SUCCESS(uuid)
            }
            is GetMojangPlayerAction.Result.FAILED -> {
                when (result.reason) {
                    GetMojangPlayerAction.Failure.DESERIALIZE_FAILED -> Result.FAILED(Failure.FETCH_FAILED)
                }
            }
        }
    }
}