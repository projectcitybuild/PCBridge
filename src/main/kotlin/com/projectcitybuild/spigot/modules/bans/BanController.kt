package com.projectcitybuild.spigot.modules.bans

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.spigot.modules.bans.commands.BanCommand

class BanController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            BanCommand()
    )
}