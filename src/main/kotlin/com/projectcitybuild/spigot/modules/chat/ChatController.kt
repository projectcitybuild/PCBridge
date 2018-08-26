package com.projectcitybuild.spigot.modules.chat

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand

class ChatController : Controller {
    override val commands: Array<Commandable> = arrayOf(
        MuteCommand()
    )
}
