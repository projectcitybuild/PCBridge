package com.projectcitybuild.pcbridge.features.bans.domain

import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.IPBan
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.models.PlayerBan
import java.time.LocalDateTime
import kotlin.random.Random

class Stubs private constructor() {
    companion object {
        fun playerBan() =
            PlayerBan(
                id = Random.Default.nextInt(),
                bannedPlayerAlias = "alias",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )

        fun ipBan() =
            IPBan(
                id = Random.Default.nextInt(),
                ipAddress = "192.168.0.1",
                bannerPlayerId = Random.Default.nextInt(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }
}
