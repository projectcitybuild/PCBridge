package com.projectcitybuild.pcbridge.features.warps

import com.projectcitybuild.pcbridge.data.SerializableLocation
import java.time.LocalDateTime

data class Warp(
    val name: String,
    val location: SerializableLocation,
    val createdAt: LocalDateTime,
)
