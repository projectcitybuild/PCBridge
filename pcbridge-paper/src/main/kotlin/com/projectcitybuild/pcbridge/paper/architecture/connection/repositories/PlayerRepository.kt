package com.projectcitybuild.pcbridge.paper.architecture.connection.repositories

import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.services.PlayerHttpService
import com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities.sanitized
import java.net.InetAddress
import java.util.UUID
import kotlin.jvm.Throws

class PlayerRepository(
    private val httpService: PlayerHttpService,
) {
    @Throws(Exception::class)
    suspend fun get(
        uuid: UUID,
        ip: InetAddress?,
    ): PlayerData = httpService.get(
        playerUUID = uuid,
        ip = ip?.sanitized(),
    )
}
