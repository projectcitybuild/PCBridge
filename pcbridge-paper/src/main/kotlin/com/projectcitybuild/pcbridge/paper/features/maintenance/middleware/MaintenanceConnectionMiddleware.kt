package com.projectcitybuild.pcbridge.paper.features.maintenance.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionMiddleware
import com.projectcitybuild.pcbridge.paper.architecture.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import net.kyori.adventure.text.minimessage.MiniMessage
import java.net.InetAddress
import java.util.UUID

class MaintenanceConnectionMiddleware(
    private val store: Store,
) : ConnectionMiddleware {
    override suspend fun handle(
        uuid: UUID,
        ip: InetAddress,
        playerData: PlayerData
    ): ConnectionResult {
        if (!store.state.maintenance) return ConnectionResult.Allowed

        if (playerData.isStaff) {
            return ConnectionResult.Allowed
        }
        val message = "<bold>Server Maintenance</bold><newline><newline><gray>Please try again later</gray>"
        return ConnectionResult.Denied(
            reason = MiniMessage.miniMessage().deserialize(message),
        )
    }
}