package com.projectcitybuild.spigot.modules.bans

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.bans.commands.BanCommand
import com.projectcitybuild.spigot.modules.bans.commands.BanStatusCommand
import com.projectcitybuild.spigot.modules.bans.commands.UnbanCommand
import com.projectcitybuild.spigot.modules.bans.listeners.BanConnectionListener

class BanController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            BanCommand(),
            UnbanCommand(),
            BanStatusCommand()
    )

    override val listeners: Array<Listenable<*>> = arrayOf(
            BanConnectionListener()
    )
}