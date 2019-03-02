package com.projectcitybuild.core.contracts

import org.bukkit.command.CommandSender

/**
 * Represents a Command that a user can use to perform
 * some kind of action.
 *
 * For example, performing a player ban would be handled by
 * BanCommand inheriting from Commandable
 */
interface Commandable : Injectable {
    val label: String
    val aliases: Array<String>
        get() = arrayOf()
    val permission: String

    fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean) : Boolean
}