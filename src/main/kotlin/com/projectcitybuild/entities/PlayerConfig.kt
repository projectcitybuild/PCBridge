package com.projectcitybuild.entities

import java.util.*

data class PlayerConfig(
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