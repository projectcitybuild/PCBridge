package com.projectcitybuild.pcbridge.paper.features.sync.domain.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.Authorization
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.services.ConnectionHttpService
import com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities.sanitized
import java.net.InetAddress
import java.util.UUID

class ConnectionRepository(
    private val httpService: ConnectionHttpService,
) {
    suspend fun auth(
        uuid: UUID,
        ip: InetAddress?,
    ): Authorization = httpService.auth(
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
