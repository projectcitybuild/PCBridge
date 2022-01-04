package com.projectcitybuild.entities

import java.util.*

data class CachedPlayer(
    val uuid: UUID,
    var isMuted: Boolean,
    var chatPrefix: String,
    var chatSuffix: String,
    var chatGroups: String,
)