package com.projectcitybuild.modules.messaging.senders

import com.projectcitybuild.modules.messaging.MessageBuilder
import com.projectcitybuild.modules.messaging.MessageReceivable
import com.projectcitybuild.modules.messaging.PlatformMessageSender
import com.projectcitybuild.modules.messaging.send
import com.projectcitybuild.modules.messaging.serializers.TextComponentSerializer
import net.md_5.bungee.api.CommandSender
import javax.inject.Inject

class BungeecordMessageSender @Inject constructor(): PlatformMessageSender {

    override fun process(
        receiver: MessageReceivable,
        builder: MessageBuilder
    ) {
        if (receiver !is BungeecordMessageReceiver) {
            return
        }
        TextComponentSerializer().serialize(builder).let {
            receiver.commandSender.sendMessage(it)
        }
    }
}

data class BungeecordMessageReceiver(
    val commandSender: CommandSender,
): MessageReceivable

inline fun PlatformMessageSender.send(
    receiver: CommandSender,
    build: MessageBuilder.() -> Unit
) {
    send(BungeecordMessageReceiver(receiver), build)
}