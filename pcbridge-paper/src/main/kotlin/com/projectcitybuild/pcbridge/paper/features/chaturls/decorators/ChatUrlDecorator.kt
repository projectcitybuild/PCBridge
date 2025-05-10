package com.projectcitybuild.pcbridge.paper.features.chaturls.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessageDecorator
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.event.ClickEvent
import java.util.regex.Pattern

class ChatUrlDecorator: ChatMessageDecorator {
    private val urlReplacer = TextReplacementConfig
        .builder()
        .match(DEFAULT_URL_PATTERN)
        .replacement { component ->
            val url = component.content().let { content ->
                if (!URL_SCHEME_PATTERN.matcher(content).find()) "https://$content"
                else content
            }
            component.clickEvent(ClickEvent.openUrl(url))
        }
        .build()

    override suspend fun decorate(prev: ChatMessage): ChatMessage {
        return prev.copy(
            message = prev.message.replaceText(urlReplacer),
        )
    }

    private companion object {
        val DEFAULT_URL_PATTERN: Pattern = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?")
        val URL_SCHEME_PATTERN: Pattern = Pattern.compile("^[a-z][a-z0-9+\\-.]*:")
    }
}
