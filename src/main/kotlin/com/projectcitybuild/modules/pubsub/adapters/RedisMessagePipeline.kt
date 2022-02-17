package com.projectcitybuild.modules.pubsub.adapters

import com.projectcitybuild.core.infrastructure.redis.RedisConnection
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.pubsub.PipelineToNodes
import com.projectcitybuild.modules.pubsub.PipelineToProxy
import com.projectcitybuild.modules.pubsub.ServerMessage
import redis.clients.jedis.JedisPubSub
import javax.inject.Inject

class RedisMessagePipeline @Inject constructor(
    private val redisConnection: RedisConnection,
): PipelineToNodes, PipelineToProxy {

    private val nodeSubscribers = HashMap<SubChannel, PipelineToNodes.Subscriber>()
    private val proxySubscribers = HashMap<SubChannel, PipelineToProxy.Subscriber>()

    override fun connect() {
        redisConnection.resource().subscribe(object: JedisPubSub() {
            override fun onMessage(channel: String?, message: String?) {
                if (channel.isNullOrEmpty()) return

                when (channel) {
                    "to_proxy" -> {

                    }
                    "to_nodes" -> {

                    }
                }
            }
        }, "to_proxy", "to_nodes")
    }

    override fun close() {
        nodeSubscribers.clear()
        proxySubscribers.clear()
    }

    override fun publishToNodes(destination: String, subChannel: SubChannel, message: ServerMessage) {
        TODO("Not yet implemented")
    }

    override fun publishToProxy(subChannel: SubChannel, message: ServerMessage) {
        TODO("Not yet implemented")
    }

    override fun subscribeToNodes(subChannel: SubChannel, subscriber: PipelineToNodes.Subscriber) {
        nodeSubscribers[subChannel] = subscriber
    }

    override fun subscribeToProxy(subChannel: SubChannel, subscriber: PipelineToProxy.Subscriber) {
        proxySubscribers[subChannel] = subscriber
    }
}