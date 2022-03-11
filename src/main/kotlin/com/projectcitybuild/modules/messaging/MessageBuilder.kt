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

    fun text(string: String) {
        tokens.add(TextToken().apply { unformatted(string) })
    }

    fun divider() {
        tokens.add(DividerToken())
    }

    fun paginated(
        numberOfPages: Int,
        currentPage: Int,
        prevPageCommand: String,
        nextPageCommand: String,
        apply: TextToken.() -> Unit
    ) {
        TODO()
    }
}