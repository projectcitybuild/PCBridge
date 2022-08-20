package com.projectcitybuild

import com.projectcitybuild.entities.PlayerConfig
import java.time.LocalDateTime
import java.util.UUID

fun PlayerConfigMock(uuid: UUID? = null): PlayerConfig {
    return PlayerConfig(
        id = 1,
        uuid = uuid ?: UUID.randomUUID(),
        isMuted = false,
        isChatBadgeDisabled = false,
        firstSeen = LocalDateTime.now(),
    )
}
