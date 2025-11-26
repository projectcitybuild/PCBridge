package com.projectcitybuild.pcbridge.paper.features.bans.hooks.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.paper.features.bans.domain.actions.CheckBan
import com.projectcitybuild.pcbridge.paper.features.bans.domain.utilities.toMiniMessage
import java.net.InetAddress
import java.util.UUID

class BanConnectionMiddleware(
    private val checkBan: CheckBan,
) : ConnectionMiddleware {
    override suspend fun handle(
        uuid: UUID,
        ip: InetAddress,
        playerData: PlayerData
    ): ConnectionResult {
        val ban = checkBan.get(playerData)
            ?: return ConnectionResult.Allowed

        return ConnectionResult.Denied(reason = ban.toMessage())
    }
}

private fun CheckBan.Ban.toMessage() = when (this) {
    is CheckBan.Ban.UUID -> value.toMiniMessage()
    is CheckBan.Ban.IP -> value.toMiniMessage()
}