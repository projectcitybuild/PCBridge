package com.projectcitybuild.features.warps

import com.projectcitybuild.data.SerializableLocation
import java.time.LocalDateTime

data class Warp(
    val name: String,
    val location: SerializableLocation,
    val createdAt: LocalDateTime,
)
