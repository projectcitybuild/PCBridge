package com.projectcitybuild.pcbridge.paper.architecture.chat.decorators

interface ChatDecorator<T> {
    suspend fun handle(prev: T): T
}

typealias ChatSenderDecorator = ChatDecorator<ChatSender>
typealias ChatMessageDecorator = ChatDecorator<ChatMessage>