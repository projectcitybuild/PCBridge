package com.projectcitybuild.entities

import java.time.LocalDateTime

data class Warp(
    val name: String = "warp_name",
    val location: SerializableLocation = SerializableLocation(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
