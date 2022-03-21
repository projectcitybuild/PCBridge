package com.projectcitybuild.platforms.bungeecord.environment

import net.md_5.bungee.api.CommandSender

interface BungeecordCommand {

    // String used to execute the command in-game (eg. "ban", "unban")
    val label: String

    // Alternative Strings to execute the command in-game
    val aliases: Array<String>
        get() = arrayOf()

    // Permission node required to execute the command
    val permission: String

    // Message shown to the user if the command input was invalid
    val usageHelp: String

    suspend fun execute(input: BungeecordCommandInput)
    fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? = null
}
