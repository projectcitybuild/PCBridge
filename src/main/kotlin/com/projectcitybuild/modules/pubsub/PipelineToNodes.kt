package com.projectcitybuild.modules.pubsub

import com.projectcitybuild.entities.SubChannel

interface PipelineToNodes {
    fun connect()
    fun close()
    fun subscribeToNodes(subChannel: SubChannel, subscriber: Subscriber)
    fun publishToNodes(destination: String, subChannel: SubChannel, message: ServerMessage)

    interface Subscriber {
        fun onReceiveMessage(message: ServerMessage)
    }
}