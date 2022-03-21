package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class Home(
    val id: Long,
    val name: String,
    val playerUUID: UUID,
    val location: CrossServerLocation,
    val createdAt: LocalDateTime,
) {
    companion object {}  // To allow static extensions
}