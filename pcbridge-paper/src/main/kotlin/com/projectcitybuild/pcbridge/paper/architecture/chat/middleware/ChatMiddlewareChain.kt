package com.projectcitybuild.pcbridge.paper.architecture.chat.middleware

class ChatMiddlewareChain(
    private val middlewares: MutableList<ChatMiddleware> = mutableListOf(),
) {
    fun register(vararg middleware: ChatMiddleware) = middleware.forEach {
        middlewares.add(it)
    }

    suspend fun pipe(chat: Chat): Chat {
        var updated = chat
        for (middleware in middlewares) {
            updated = middleware.handle(updated)
        }
        return updated
    }
}