package com.projectcitybuild.stubs

import com.projectcitybuild.entities.responses.IPBan
import java.util.*

fun IPBanMock(): IPBan {
    return IPBan(
        id = Math.random().toInt(),
        bannerPlayerId = UUID.randomUUID().toString(),
        reason = "reason",
        createdAt = 1642958606,
        updatedAt = 1642958606,
        unbannedAt = null,
        unbannerPlayerId = null,
        unbanType = null,
    )
}
