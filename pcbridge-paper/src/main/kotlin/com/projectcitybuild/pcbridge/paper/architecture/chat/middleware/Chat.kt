package com.projectcitybuild.pcbridge.paper.architecture.chat.middleware

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

data class Chat(
    val source: Player,
    val sourceDisplayName: Component,
    val message: Component,
)