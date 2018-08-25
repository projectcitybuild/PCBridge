package com.projectcitybuild.spigot.modules.bans

import com.projectcitybuild.core.protocols.Commandable
import com.projectcitybuild.core.protocols.Controller
import com.projectcitybuild.spigot.modules.bans.commands.BanCommand

class BanController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            BanCommand()
    )
}