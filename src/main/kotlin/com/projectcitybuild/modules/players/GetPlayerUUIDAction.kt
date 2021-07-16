package com.projectcitybuild.modules.players

import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Result
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.platforms.bungeecord.extensions.addDashesToUUID
import net.md_5.bungee.api.ProxyServer
import java.util.*

class GetPlayerUUIDAction(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
) {
    sealed class FailReason {
        class API_ERROR(val message: String): FailReason()
    }

    fun execute(
            playerName: String,
            timestamp: Long? = null,
            proxyServer: ProxyServer,
            completion: (Result<UUID, FailReason>) -> Unit
    ) {
        // Search for online player first
        val onlinePlayer = proxyServer.getPlayer(playerName)
        if (onlinePlayer != null) {
            completion(Success(onlinePlayer.uniqueId))
            return
        }

        // Otherwise fetch from Mojang's API
        val mojangApi = apiRequestFactory.mojang.mojangApi
        val request = mojangApi.getMojangPlayer(playerName, timestamp = timestamp)
        apiClient.executeMojang(request).startAndSubscribe { result ->
            when (result) {
                is Success -> completion(Success(UUID.fromString(result.value.uuid.addDashesToUUID())))
                is Failure -> completion(Failure(FailReason.API_ERROR(result.reason.message)))
            }
        }
    }
}