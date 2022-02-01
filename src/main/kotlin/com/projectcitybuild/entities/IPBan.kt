package com.projectcitybuild.entities

import java.time.LocalDateTime

data class IPBan(
    val ip: String,
    val bannerName: String,
    val reason: String?,
    val createdAt: LocalDateTime,
)