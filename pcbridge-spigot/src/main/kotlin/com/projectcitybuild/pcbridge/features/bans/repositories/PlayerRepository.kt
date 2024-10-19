package com.projectcitybuild.pcbridge.features.bans.repositories

import com.projectcitybuild.pcbridge.features.bans.Sanitizer
import com.projectcitybuild.pcbridge.http.responses.PlayerData
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerHttpService
import java.net.InetAddress
import java.util.UUID
import kotlin.jvm.Throws

class PlayerRepository(
    private val httpService: PlayerHttpService,
) {
    @Throws(Exception::class)
    suspend fun get(
        playerUUID: UUID,
        ip: InetAddress,
    ): PlayerData {
        return httpService.get(
            playerUUID = playerUUID,
            ip = Sanitizer.sanitizedIP(ip.toString()),
        )
    }
}
