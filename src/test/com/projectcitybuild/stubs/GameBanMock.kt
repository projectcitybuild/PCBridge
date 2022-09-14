package com.projectcitybuild

import com.projectcitybuild.entities.responses.GameBan
import java.util.UUID

fun GameBanMock(): GameBan {
    return GameBan(
        id = 1,
        serverId = 2,
        bannedPlayerId = UUID.randomUUID().toString(),
        bannedPlayerAlias = "alias",
        bannerPlayerId = UUID.randomUUID().toString(),
        reason = "reason",
        createdAt = 1642958606,
        updatedAt = 1642958606,
        expiresAt = null,
        unbannedAt = null,
        unbannerPlayerId = null,
        unbanType = null,
    )
}
