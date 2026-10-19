package com.projectcitybuild.pcbridge.features.maintenance.hooks.middleware

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Authorization
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionMiddleware
import com.projectcitybuild.pcbridge.runtime.connection.middleware.ConnectionResult
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import java.net.InetAddress
import java.util.UUID

class MaintenanceConnectionMiddleware(
    private val store: Store,
) : ConnectionMiddleware {
    override suspend fun handle(
        uuid: UUID,
        ip: InetAddress,
        authorization: Authorization,
    ): ConnectionResult {
        if (!store.state(maintenanceStateKey).enabled) return ConnectionResult.Allowed

        val playerData = authorization.player
        if (playerData != null && playerData.isStaff) {
            return ConnectionResult.Allowed
        }
        return ConnectionResult.Denied(
            reason = l10n.maintenance.maintenanceConnectionDenied,
        )
    }
}
