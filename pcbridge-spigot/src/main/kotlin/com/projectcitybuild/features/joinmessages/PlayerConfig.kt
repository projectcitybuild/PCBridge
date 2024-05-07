package com.projectcitybuild.features.joinmessages

import java.time.LocalDateTime
import java.util.UUID

data class PlayerConfig(
    val id: Long,
    val uuid: UUID,
    val firstSeen: LocalDateTime,
)
