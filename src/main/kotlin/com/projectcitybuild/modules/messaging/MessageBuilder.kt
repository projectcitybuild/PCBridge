package com.projectcitybuild.modules.messaging

import com.projectcitybuild.modules.messaging.components.MessageStyle
import com.projectcitybuild.modules.messaging.components.Text

class MessageBuilder {
    sealed class Token {

    }

    private val tokens: MutableList<Token> = mutableListOf()

    var style: MessageStyle = MessageStyle.NORMAL

    fun text(apply: Text.() -> Unit) {
        tokens.add(Text().apply(apply))
    }

    fun divider() {

    }
}