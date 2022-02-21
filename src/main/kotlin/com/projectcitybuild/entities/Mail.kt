package com.projectcitybuild.entities

import java.time.LocalDateTime
import java.util.*

data class Mail(
    val id: Long,
    val senderUUID: UUID,
    val senderName: String,
    val receiverUUID: UUID,
    val receiverName: String,
    val message: String,
    val isRead: Boolean,
    val isCleared: Boolean,
    val createdAt: LocalDateTime,
    val readAt: LocalDateTime?,
    val clearedAt: LocalDateTime?,
)
