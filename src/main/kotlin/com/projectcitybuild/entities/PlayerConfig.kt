package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class PlayerConfig(
    val id: Long,
    val uuid: UUID,
    var isMuted: Boolean,
    var isAllowingTPs: Boolean,
    val firstSeen: LocalDateTime
)