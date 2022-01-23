package com.projectcitybuild.modules.proxyadapter.kick

import java.util.*

interface PlayerKicker {
    enum class KickContext {
        FATAL,
    }

    fun kick(
        playerName: String,
        reason: String,
        context: KickContext = KickContext.FATAL
    )

    fun kick(
        playerUUID: UUID,
        reason: String,
        context: KickContext = KickContext.FATAL
    )
}