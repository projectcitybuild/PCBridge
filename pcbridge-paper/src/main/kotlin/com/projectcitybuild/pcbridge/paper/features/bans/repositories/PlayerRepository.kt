package com.projectcitybuild.pcbridge.paper.features.bans.repositories

import com.projectcitybuild.pcbridge.paper.features.bans.utilities.Sanitizer
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerData
import com.projectcitybuild.pcbridge.http.pcb.services.PlayerHttpService
import java.net.InetAddress
import java.util.UUID
import kotlin.jvm.Throws

class PlayerRepository(
    private val httpService: PlayerHttpService,
) {
    @Throws(Exception::class)
    suspend fun get(
        playerUUID: UUID,
        ip: InetAddress?,
    ): PlayerData {
        return httpService.get(
            playerUUID = playerUUID,
            ip = ip?.let { Sanitizer.sanitizedIP(it.toString()) },
        )
    }
}
