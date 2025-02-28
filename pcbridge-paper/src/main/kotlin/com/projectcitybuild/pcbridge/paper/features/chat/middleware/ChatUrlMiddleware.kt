package com.projectcitybuild.pcbridge.paper.features.chat.middleware

import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.Chat
import com.projectcitybuild.pcbridge.paper.architecture.chat.middleware.ChatMiddleware
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class ChatUrlMiddleware : ChatMiddleware {
    override suspend fun handle(chat: Chat): Chat {
        // Only the legacy serializer automatically converts URLs to clickable text
        val legacySerializer = LegacyComponentSerializer
            .builder()
            .extractUrls()
            .build()

        return chat.copy(
            message = legacySerializer.deserialize(
                legacySerializer.serialize(chat.message)
            )
        )
    }
}
