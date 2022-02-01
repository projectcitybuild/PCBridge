package com.projectcitybuild

import com.projectcitybuild.entities.responses.GameBan
import java.util.*

fun GameBanMock(): GameBan {
    return GameBan(
        id = 1,
        serverId = 2,
        playerId = UUID.randomUUID().toString(),
        playerType = "minecraft_player",
        playerAlias = "alias",
        staffId = UUID.randomUUID().toString(),
        staffType = "minecraft_player",
        reason = "reason",
        isActive = true,
        isGlobalBan = true,
        createdAt = 1642958606,
        updatedAt = 1642958606,
        expiresAt = null,
    )
}