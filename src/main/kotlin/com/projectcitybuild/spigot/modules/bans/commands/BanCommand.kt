package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.command.CommandSender

class BanCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "ban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        sender?.sendMessage("Hello ${sender.name}")
        System.out.println("TEST")

        return true
    }

}