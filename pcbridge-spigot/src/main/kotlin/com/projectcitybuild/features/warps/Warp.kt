package com.projectcitybuild.features.warps

import com.projectcitybuild.entities.SerializableLocation
import java.time.LocalDateTime

data class Warp(
    val name: String,
    val location: SerializableLocation,
    val createdAt: LocalDateTime,
)
