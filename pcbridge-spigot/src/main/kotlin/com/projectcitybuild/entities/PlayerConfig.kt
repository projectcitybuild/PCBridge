package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.UUID

data class PlayerConfig(
    val id: Long,
    val uuid: UUID,
    var isMuted: Boolean,
    var isChatBadgeDisabled: Boolean,
    val firstSeen: LocalDateTime,
)
