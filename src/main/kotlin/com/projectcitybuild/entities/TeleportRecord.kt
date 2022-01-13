package com.projectcitybuild.entities

import com.projectcitybuild.entities.serializables.SerializableDate
import com.projectcitybuild.entities.serializables.SerializableLocation
import kotlinx.serialization.Serializable

@Serializable
data class TeleportRecord(
    val location: SerializableLocation,
    val date: SerializableDate,
    val reason: TeleportReason,
)

enum class TeleportReason {
    TP_TO,
    TP_SUMMONED,
    WARPED,
}