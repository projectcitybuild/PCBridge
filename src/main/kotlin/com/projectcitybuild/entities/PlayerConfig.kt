package com.projectcitybuild.entities

import com.projectcitybuild.modules.storage.SerializableUUID
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlayerConfig(
    val uuid: SerializableUUID,
    var isMuted: Boolean,
    var chatPrefix: String,
    var chatSuffix: String,
    var chatGroups: String,
    val chatIgnoreList: MutableSet<SerializableUUID>
) {
    val unwrappedChatIgnoreList
        get() = chatIgnoreList.map { it.unwrapped }

    companion object {
        fun default(uuid: UUID) : PlayerConfig = PlayerConfig(
            uuid = SerializableUUID(uuid),
            isMuted = false,
            chatSuffix = "",
            chatPrefix = "",
            chatGroups = "",
            chatIgnoreList = mutableSetOf()
        )
    }
}