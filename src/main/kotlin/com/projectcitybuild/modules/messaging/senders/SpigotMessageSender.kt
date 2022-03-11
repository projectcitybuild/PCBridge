package com.projectcitybuild.modules.messaging.senders

import com.projectcitybuild.modules.messaging.MessageBuilder
import com.projectcitybuild.modules.messaging.MessageReceivable
import com.projectcitybuild.modules.messaging.PlatformMessageSender
import com.projectcitybuild.modules.messaging.send
import com.projectcitybuild.modules.messaging.serializers.TextComponentSerializer
import org.bukkit.command.CommandSender
import javax.inject.Inject

class SpigotMessageSender @Inject constructor(): PlatformMessageSender {

    override fun process(
        receiver: MessageReceivable,
        builder: MessageBuilder
    ) {
        if (receiver !is SpigotMessageReceiver) {
            return
        }
        TextComponentSerializer().serialize(builder).let {
            receiver.commandSender.spigot().sendMessage(it)
        }
    }
}

data class SpigotMessageReceiver(
    val commandSender: CommandSender,
): MessageReceivable

inline fun PlatformMessageSender.send(
    receiver: CommandSender,
    build: MessageBuilder.() -> Unit
) {
    send(SpigotMessageReceiver(receiver), build)
}