package com.projectcitybuild.pcbridge.runtime.connection

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.Authorization
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.ConnectionHttpService
import com.projectcitybuild.pcbridge.platform.paper.utilities.sanitized
import java.net.InetAddress
import java.util.UUID

class ConnectionRepository(
    private val httpService: ConnectionHttpService,
) {
    suspend fun auth(
        uuid: UUID,
        ip: InetAddress?,
    ): Authorization =
        httpService.auth(
            playerUUID = uuid,
            ip = ip?.sanitized(),
        )

    suspend fun end(
        uuid: UUID,
        sessionSeconds: Long,
    ) = httpService.end(
        playerUUID = uuid,
        sessionSeconds = sessionSeconds,
    )
}
