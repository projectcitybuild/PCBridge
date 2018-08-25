package com.projectcitybuild.core.protocols

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

interface Commandable : Injectable {
    val label: String
    val aliases: Array<String>
        get() = arrayOf()

    fun execute(sender: CommandSender?, command: Command?, label: String?, args: Array<String>?) : Boolean
}