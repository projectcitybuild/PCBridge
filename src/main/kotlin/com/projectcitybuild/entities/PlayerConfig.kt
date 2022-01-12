package com.projectcitybuild.entities

import com.projectcitybuild.old_modules.storage.serializers.SerializableUUID
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlayerConfig(
    val uuid: SerializableUUID,
    var isMuted: Boolean = false,
    var isAllowingTPs: Boolean = true,
    var chatPrefix: String = "",
    var chatSuffix: String = "",
    var chatGroups: String = "",
    val chatIgnoreList: MutableSet<SerializableUUID> = mutableSetOf()
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