package com.projectcitybuild.pcbridge.features.bans.actions

import com.projectcitybuild.pcbridge.http.responses.Aggregate
import com.projectcitybuild.pcbridge.http.responses.IPBan
import com.projectcitybuild.pcbridge.http.responses.PlayerBan

class AuthoriseConnection {
    sealed class ConnectResult {
        object Allowed : ConnectResult()
        data class Denied(val ban: Ban) : ConnectResult()
    }

    sealed class Ban {
        data class UUID(val value: PlayerBan) : Ban()
        data class IP(val value: IPBan) : Ban()
    }

    fun execute(aggregate: Aggregate): ConnectResult {
        val ban = getBan(aggregate)
        if (ban != null) {
            return ConnectResult.Denied(ban = ban)
        }
        return ConnectResult.Allowed
    }

    private fun getBan(aggregate: Aggregate): Ban? {
        val playerBan = aggregate.playerBan
        if (playerBan != null && playerBan.isActive) {
            return Ban.UUID(playerBan)
        }
        val ipBan = aggregate.ipBan
        if (ipBan != null && ipBan.isActive) {
            return Ban.IP(ipBan)
        }
        return null
    }
}
