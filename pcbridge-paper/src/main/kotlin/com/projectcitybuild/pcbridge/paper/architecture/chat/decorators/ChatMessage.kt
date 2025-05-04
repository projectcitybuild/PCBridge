package com.projectcitybuild.pcbridge.paper.architecture.chat.decorators

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

data class ChatMessage(
    private val player: Player,
    val message: Component,
) {
    val sender: Player
        get() = player
}

data class ChatSender(
    private val player: Player,
    val sourceDisplayName: Component,
) {
    val sender: Player
        get() = player
}