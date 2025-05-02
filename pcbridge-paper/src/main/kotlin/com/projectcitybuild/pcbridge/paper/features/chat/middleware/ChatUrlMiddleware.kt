package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMessage
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ChatUrlMiddleware : ChatMiddleware {
    override suspend fun handle(chatMessage: ChatMessage): ChatMessage {
        // Only the legacy serializer automatically converts URLs to clickable text
        val legacySerializer = LegacyComponentSerializer
            .builder()
            .extractUrls()
            .build()

        return chatMessage.copy(
            message = legacySerializer.deserialize(
                legacySerializer.serialize(chatMessage.message)
            )
        )
    }
}
