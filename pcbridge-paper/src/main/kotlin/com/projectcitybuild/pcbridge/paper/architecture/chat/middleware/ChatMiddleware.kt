package com.projectcitybuild.pcbridge.paper.architecture.chat.middleware

interface ChatMiddleware {
    suspend fun handle(chat: Chat): Chat
}
