package com.projectcitybuild.pcbridge.paper.architecture.chat.decorators

class ChatDecoratorChain(
    private val senderDecorators: MutableList<ChatSenderDecorator> = mutableListOf(),
    private val messageDecorators: MutableList<ChatMessageDecorator> = mutableListOf(),
) {
    fun addSender(vararg middleware: ChatSenderDecorator)
        = middleware.forEach { senderDecorators.add(it) }

    fun addMessage(vararg middleware: ChatMessageDecorator)
        = middleware.forEach { messageDecorators.add(it) }

    suspend fun pipe(sender: ChatSender): ChatSender {
        var updated = sender
        for (decorator in senderDecorators) {
            updated = decorator.handle(updated)
        }
        return updated
    }

    suspend fun pipe(message: ChatMessage): ChatMessage {
        var updated = message
        for (decorator in messageDecorators) {
            updated = decorator.handle(updated)
        }
        return updated
    }
}
