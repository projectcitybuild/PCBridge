package com.projectcitybuild.pcbridge.paper

import com.projectcitybuild.pcbridge.http.pcb.models.IPBan
import com.projectcitybuild.pcbridge.http.pcb.models.PlayerBan
import java.time.LocalDateTime
import kotlin.random.Random

class Stubs private constructor() {
    companion object {
        fun playerBan() = PlayerBan(
            id = Random.nextInt(),
            bannedPlayerAlias = "alias",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        fun ipBan() = IPBan(
            id = Random.nextInt(),
            ipAddress = "192.168.0.1",
            bannerPlayerId = Random.nextInt(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )
    }
}