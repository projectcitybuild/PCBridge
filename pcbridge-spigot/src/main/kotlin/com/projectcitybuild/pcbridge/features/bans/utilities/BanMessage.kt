package com.projectcitybuild.pcbridge.features.bans.utilities

import com.projectcitybuild.pcbridge.http.models.IPBan
import com.projectcitybuild.pcbridge.http.models.PlayerBan
import net.kyori.adventure.text.minimessage.MiniMessage

fun PlayerBan.toMiniMessage() = MiniMessage.miniMessage().deserialize(
    """
        <color:red><b>You are currently banned.</b></color>
                
        <color:gray>Reason:</color> ${reason ?: "No reason provided"}
        <color:gray>Expires:</color> ${expiresAt ?: "Never"}
                
        <color:aqua>Appeal @ https://projectcitybuild.com"</color>
    """.trimIndent(),
)

fun IPBan.toMiniMessage() = MiniMessage.miniMessage().deserialize(
    """
        <color:red><b>You are currently IP banned.</b></color>
                
        <color:gray>Reason:</color> ${reason ?: "No reason provided" }
                
        <color:aqua>Appeal @ https://projectcitybuild.com</color>
    """.trimIndent(),
)