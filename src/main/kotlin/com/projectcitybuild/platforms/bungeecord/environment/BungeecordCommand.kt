package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.entities.CommandResult
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

data class BungeecordCommandInput(
        val sender: CommandSender,
        val args: List<String>
) {
    val isConsoleSender = sender is ProxiedPlayer
    val player = sender as? ProxiedPlayer

    var hasArguments: Boolean = args.isNotEmpty()
}

interface BungeecordCommand {

    // String used to execute the command in-game (eg. "ban", "unban")
    val label: String

    // Alternative Strings to execute the command in-game
    val aliases: Array<String>
        get() = arrayOf()

    // Permission node required to execute the command
    val permission: String

    fun validate(input: BungeecordCommandInput) : CommandResult

    suspend fun execute(input: BungeecordCommandInput)
}