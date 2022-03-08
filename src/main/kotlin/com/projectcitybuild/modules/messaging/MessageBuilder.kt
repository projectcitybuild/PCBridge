package com.projectcitybuild.modules.messaging

import com.projectcitybuild.modules.messaging.components.MessageStyle
import com.projectcitybuild.modules.messaging.tokens.DividerToken
import com.projectcitybuild.modules.messaging.tokens.TextToken

class MessageBuilder {
    val tokens: MutableList<MessageBuilderToken> = mutableListOf()

    var style: MessageStyle = MessageStyle.NORMAL

    fun text(apply: TextToken.() -> Unit) {
        tokens.add(TextToken().apply(apply))
    }

    fun divider() {
        tokens.add(DividerToken())
    }
}