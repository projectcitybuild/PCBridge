package com.projectcitybuild.core.contracts

import org.bukkit.command.CommandSender

interface Commandable : Injectable {
    val label: String
    val aliases: Array<String>
        get() = arrayOf()

    fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean) : Boolean
}