package com.projectcitybuild.pcbridge.features.bans.domain.actions

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Authorization
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.IPBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan

class CheckBan {
    sealed class Ban {
        data class UUID(val value: PlayerBan) : Ban()

        data class IP(val value: IPBan) : Ban()
    }

    fun check(authorization: Authorization): Ban? {
        val playerBan = authorization.bans?.uuid
        if (playerBan != null && playerBan.isActive) {
            return Ban.UUID(playerBan)
        }
        val ipBan = authorization.bans?.ip
        if (ipBan != null && ipBan.isActive) {
            return Ban.IP(ipBan)
        }
        return null
    }
}
