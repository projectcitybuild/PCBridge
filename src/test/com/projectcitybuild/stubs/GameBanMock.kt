package com.projectcitybuild

import com.projectcitybuild.entities.responses.GameBan
import java.util.UUID

fun GameBanMock(): GameBan {
    return GameBan(
        id = 1,
        serverId = 2,
        bannedPlayerId = UUID.randomUUID().toString(),
        playerType = "minecraft_player",
        bannedPlayerAlias = "alias",
        bannerPlayerId = UUID.randomUUID().toString(),
        staffType = "minecraft_player",
        reason = "reason",
        isActive = true,
        isGlobalBan = true,
        createdAt = 1642958606,
        updatedAt = 1642958606,
        expiresAt = null,
    )
}
