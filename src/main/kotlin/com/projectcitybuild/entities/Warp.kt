package com.projectcitybuild.entities

import java.time.LocalDateTime

data class Warp(
    val name: String,
    val location: CrossServerLocation,
    val createdAt: LocalDateTime,
)