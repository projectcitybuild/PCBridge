package com.projectcitybuild.spigot.modules.ranks

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.ranks.commands.LoginCommand

class RankController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            LoginCommand()
    )

    override val listeners: Array<Listenable<*>> = arrayOf(
    )
}
