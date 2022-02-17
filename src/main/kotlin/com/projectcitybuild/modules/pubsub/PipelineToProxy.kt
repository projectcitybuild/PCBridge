package com.projectcitybuild.modules.pubsub

import com.projectcitybuild.entities.SubChannel

interface PipelineToProxy {
    fun connect()
    fun close()
    fun subscribeToProxy(subChannel: SubChannel, subscriber: Subscriber)
    fun publishToProxy(subChannel: SubChannel, message: ServerMessage)

    interface Subscriber {
        fun onReceiveMessage(message: ServerMessage)
    }
}