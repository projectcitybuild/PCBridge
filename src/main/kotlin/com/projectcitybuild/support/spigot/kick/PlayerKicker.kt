package com.projectcitybuild.support.spigot.kick

import java.util.UUID

interface PlayerKicker {
    enum class KickContext {
        FATAL,
    }

    fun kickByName(
        playerName: String,
        reason: String,
        context: KickContext = KickContext.FATAL,
    )

    fun kickByUUID(
        playerUUID: UUID,
        reason: String,
        context: KickContext = KickContext.FATAL,
    )

    fun kickByIP(
        ip: String,
        reason: String,
        context: KickContext = KickContext.FATAL,
    )
}
