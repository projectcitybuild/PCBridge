package com.projectcitybuild.support.spigot.commands

import org.bukkit.command.CommandSender

/**
 * Represents a Command that a user can use to perform
 * some kind of action.
 *
 * For example, performing a player ban would be handled by
 * BanCommand inheriting from Commandable
 */
interface SpigotCommand {

    // String used to execute the command in-game (eg. "ban", "unban")
    val label: String

    // Alternative Strings to execute the command in-game
    val aliases: Array<String>
        get() = arrayOf()

    // Permission node required to execute the command
    val permission: String

    // Message shown to the user if the command input was invalid
    val usageHelp: String

    suspend fun execute(input: SpigotCommandInput)

    fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? = null
}
