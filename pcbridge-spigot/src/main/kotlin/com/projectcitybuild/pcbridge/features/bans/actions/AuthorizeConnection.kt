package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.http.models.PlayerData
import com.projectcitybuild.pcbridge.http.models.IPBan
import com.projectcitybuild.pcbridge.http.models.PlayerBan

class AuthorizeConnection {
    sealed class ConnectResult {
        object Allowed : ConnectResult()

        data class Denied(val ban: Ban) : ConnectResult()
    }

    sealed class Ban {
        data class UUID(val value: PlayerBan) : Ban()

        data class IP(val value: IPBan) : Ban()
    }

    fun authorize(playerData: PlayerData): ConnectResult {
        val ban = getBan(playerData)
        if (ban != null) {
            return ConnectResult.Denied(ban = ban)
        }
        return ConnectResult.Allowed
    }

    private fun getBan(playerData: PlayerData): Ban? {
        val playerBan = playerData.playerBan
        if (playerBan != null && playerBan.isActive) {
            return Ban.UUID(playerBan)
        }
        val ipBan = playerData.ipBan
        if (ipBan != null && ipBan.isActive) {
            return Ban.IP(ipBan)
        }
        return null
    }
}
