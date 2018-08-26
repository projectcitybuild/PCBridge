package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.protocols.Commandable
import com.projectcitybuild.core.protocols.Environment
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class BanCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "ban"

    override fun execute(sender: CommandSender?, command: Command?, label: String?, args: Array<String>?): Boolean {
        sender?.sendMessage("Hello ${sender.name}")
        System.out.println("TEST")

        return true
    }

}