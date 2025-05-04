package com.projectcitybuild.pcbridge.paper.architecture.chat.decorators

class ChatDecoratorChain(
    private val senderDecorators: MutableList<ChatSenderDecorator> = mutableListOf(),
    private val messageDecorators: MutableList<ChatMessageDecorator> = mutableListOf(),
) {
    fun addSender(vararg decorator: ChatSenderDecorator)
        = decorator.forEach { senderDecorators.add(it) }

    fun addMessage(vararg decorator: ChatMessageDecorator)
        = decorator.forEach { messageDecorators.add(it) }

    suspend fun pipe(sender: ChatSender): ChatSender {
        var updated = sender
        for (decorator in senderDecorators) {
            updated = decorator.decorate(updated)
        }
        return updated
    }

    suspend fun pipe(message: ChatMessage): ChatMessage {
        var updated = message
        for (decorator in messageDecorators) {
            updated = decorator.decorate(updated)
        }
        return updated
    }
}
