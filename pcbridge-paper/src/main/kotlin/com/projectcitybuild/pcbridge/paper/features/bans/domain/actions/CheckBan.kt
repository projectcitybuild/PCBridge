package com.projectcitybuild.pcbridge.paper.features.bans.domain.actions

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan

class CheckBan {
    sealed class Ban {
        data class UUID(val value: PlayerBan) : Ban()
        data class IP(val value: IPBan) : Ban()
    }

    fun check(playerData: PlayerData): Ban? {
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
