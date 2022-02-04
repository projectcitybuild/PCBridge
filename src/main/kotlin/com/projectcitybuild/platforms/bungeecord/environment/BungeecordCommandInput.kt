package com.projectcitybuild.platforms.bungeecord.environment

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

data class BungeecordCommandInput(
    val sender: CommandSender,
    val args: List<String>
) {
    val isConsoleSender = sender !is ProxiedPlayer
    val player = sender as? ProxiedPlayer
}