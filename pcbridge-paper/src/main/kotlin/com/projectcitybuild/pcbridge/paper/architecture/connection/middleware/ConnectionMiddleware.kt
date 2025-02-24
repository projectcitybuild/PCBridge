package com.projectcitybuild.pcbridge.paper.architecture.connection.middleware

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import java.util.UUID

interface ConnectionMiddleware {
    suspend fun handle(
        uuid: UUID,
        ip: java.net.InetAddress,
        playerData: PlayerData,
    ): ConnectionResult
}
