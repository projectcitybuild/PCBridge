package com.projectcitybuild.features.bans

import net.md_5.bungee.api.chat.TextComponent
import java.util.*

interface PlayerKicker {
    fun kick(playerName: String, reason: TextComponent)
    fun kick(playerUUID: UUID, reason: TextComponent)
}