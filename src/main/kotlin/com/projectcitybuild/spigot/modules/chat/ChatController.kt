package com.projectcitybuild.spigot.modules.chat

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand
import com.projectcitybuild.spigot.modules.chat.commands.PrefixCommand
import com.projectcitybuild.spigot.modules.chat.commands.SuffixCommand
import com.projectcitybuild.spigot.modules.chat.commands.UnmuteCommand
import com.projectcitybuild.spigot.modules.chat.listeners.ChatListener

class ChatController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            MuteCommand(),
            UnmuteCommand(),
            PrefixCommand(),
            SuffixCommand()
    )

    override val listeners: Array<Listenable<*>> = arrayOf(
            ChatListener()
    )
}
