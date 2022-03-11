package com.projectcitybuild.modules.messaging

import com.projectcitybuild.modules.messaging.components.Color
import com.projectcitybuild.modules.messaging.components.MessageStyle

interface PlatformMessageSender {

    /**
     * Interprets a MessageBuilder and sends the formatted version
     * as a message to the given receiver
     */
    fun process(
        receiver: MessageReceivable,
        builder: MessageBuilder
    )
}

/**
 * Sends a message to the given player UUID
 */
inline fun PlatformMessageSender.send(
    receiver: MessageReceivable,
    build: MessageBuilder.() -> Unit
) {
    val builder = MessageBuilder()
    build(builder)
    process(receiver, builder)
}

/**
 * Sends a message to the given player UUID as "feedback" for
 * the user performing some kind of action.
 *
 * For example, we can send a "You teleported to XXX" message
 * upon the user teleporting
 */
fun PlatformMessageSender.notifyOfAction(
    receiver: MessageReceivable,
    message: String,
) {
    send(receiver) {
        style = MessageStyle.NORMAL

        text {
            color(Color.GRAY).italic(message)
        }
    }
}