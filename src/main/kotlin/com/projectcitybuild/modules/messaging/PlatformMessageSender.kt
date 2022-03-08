package com.projectcitybuild.modules.messaging

import com.projectcitybuild.modules.messaging.components.Color
import com.projectcitybuild.modules.messaging.components.MessageStyle
import java.util.*

interface PlatformMessageSender {

    /**
     * Interprets a MessageBuilder and sends the formatted version
     * as a message to the given player UUID
     */
    fun process(
        playerUUID: UUID,
        builder: MessageBuilder
    )
}

/**
 * Sends a message to the given player UUID
 */
inline fun PlatformMessageSender.send(
    playerUUID: UUID,
    build: MessageBuilder.() -> Unit
) {
    val builder = MessageBuilder()
    build(builder)
    process(playerUUID, builder)
}

/**
 * Sends a message to the given player UUID as "feedback" for
 * the user performing some kind of action.
 *
 * For example, we can send a "You teleported to XXX" message
 * upon the user teleporting
 */
fun PlatformMessageSender.notifyOfAction(
    playerUUID: UUID,
    message: String,
) {
    send(playerUUID) {
        style = MessageStyle.NORMAL

        text {
            color(Color.GRAY).italic(message)
        }
    }
}