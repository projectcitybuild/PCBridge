package com.projectcitybuild.entities

import com.projectcitybuild.modules.storage.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlayerConfig(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    var isMuted: Boolean,
    var chatPrefix: String,
    var chatSuffix: String,
    var chatGroups: String,
) {
    companion object {
        fun default(uuid: UUID) : PlayerConfig = PlayerConfig(
            uuid = uuid,
            isMuted = false,
            chatSuffix = "",
            chatPrefix = "",
            chatGroups = "",
        )
    }
}