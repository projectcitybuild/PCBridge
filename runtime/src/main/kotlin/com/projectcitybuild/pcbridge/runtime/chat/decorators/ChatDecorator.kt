package com.projectcitybuild.pcbridge.runtime.chat.decorators

interface ChatDecorator<T> {
    suspend fun decorate(prev: T): T
}

typealias ChatSenderDecorator = ChatDecorator<ChatSender>
typealias ChatMessageDecorator = ChatDecorator<ChatMessage>
