package com.projectcitybuild.modules.pubsub

import com.projectcitybuild.entities.SubChannel

interface PipelineToNodes {
    fun connect()
    fun close()
    fun <Message: ServerMessage> subscribeToNodes(subChannel: SubChannel, subscriber: Subscriber<Message>)
    fun publishToNodes(destination: String, subChannel: SubChannel, message: ServerMessage)

    interface Subscriber<Message: ServerMessage> {
        fun onReceiveMessage(message: Message)
    }
}