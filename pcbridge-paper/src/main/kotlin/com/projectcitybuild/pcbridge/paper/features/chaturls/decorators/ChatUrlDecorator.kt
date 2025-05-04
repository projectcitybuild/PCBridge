package com.projectcitybuild.pcbridge.paper.features.chaturls.decorators

import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.decorators.ChatMessageDecorator
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ChatUrlDecorator: ChatMessageDecorator {
    override suspend fun handle(prev: ChatMessage): ChatMessage {
        // Only the legacy serializer automatically converts URLs to clickable text
        val legacySerializer = LegacyComponentSerializer
            .builder()
            .extractUrls()
            .build()

        return prev.copy(
            message = legacySerializer.deserialize(
                legacySerializer.serialize(prev.message)
            )
        )
    }
}
