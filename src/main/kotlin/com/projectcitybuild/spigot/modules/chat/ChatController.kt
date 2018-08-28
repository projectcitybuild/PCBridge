package com.projectcitybuild.spigot.modules.chat

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Controller
import com.projectcitybuild.core.contracts.Listenable
import com.projectcitybuild.spigot.modules.chat.commands.MuteCommand
import com.projectcitybuild.spigot.modules.chat.listeners.ChatListener
import org.bukkit.event.Event
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatController : Controller {
    override val commands: Array<Commandable> = arrayOf(
            MuteCommand()
    )

//    override val listeners: Array<Listenable<Event>> = arrayOf(
//            ChatListener<AsyncPlayerChatEvent>()
//    )
}
