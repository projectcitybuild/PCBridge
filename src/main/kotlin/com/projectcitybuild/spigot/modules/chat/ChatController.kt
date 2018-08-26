package com.projectcitybuild.spigot.modules.chat

import com.projectcitybuild.core.protocols.Commandable
import com.projectcitybuild.core.protocols.Controller
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand

class ChatController : Controller {
    override val commands: Array<Commandable> = arrayOf(
        MuteCommand()
    )
}
