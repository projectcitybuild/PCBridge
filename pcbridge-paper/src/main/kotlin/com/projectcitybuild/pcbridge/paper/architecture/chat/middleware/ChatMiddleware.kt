package com.projectcitybuild.pcbridge.paper.architecture.chat.middleware

interface ChatMiddleware {
    suspend fun handle(chatMessage: ChatMessage): ChatMessage
}
