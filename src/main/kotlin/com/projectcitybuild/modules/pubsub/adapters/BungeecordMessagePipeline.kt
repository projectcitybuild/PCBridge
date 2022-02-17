package com.projectcitybuild.modules.pubsub.adapters

import com.google.common.io.ByteStreams
import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.pubsub.PipelineToNodes
import com.projectcitybuild.modules.pubsub.ServerMessage
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import javax.inject.Inject

class BungeecordMessagePipeline @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
): PipelineToNodes, BungeecordListener {
    private val listeners = HashMap<String, PipelineToNodes.Subscriber>()

    override fun connect() {
        plugin.proxy.registerChannel(Channel.BUNGEECORD)
        plugin.proxy.pluginManager?.registerListener(plugin, this)
    }

    override fun subscribeToNodes(subChannel: SubChannel, subscriber: PipelineToNodes.Subscriber) {
        listeners[subChannel.toString()] = subscriber
    }

    override fun publishToNodes(destination: String, subChannel: SubChannel, message: ServerMessage) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(subChannel.toString())

        for (param in params) {
            when (param) {
                is String -> out.writeUTF(param)
                is Int -> out.writeInt(param)
                is Double -> out.writeDouble(param)
                is Float -> out.writeFloat(param)
                is Boolean -> out.writeBoolean(param)
                is Short -> out.writeShort(param.toInt())
                is Long -> out.writeLong(param)
                is Byte -> out.writeByte(param.toInt())
                is Char -> out.writeChar(param.code)
            }
        }
        serverInfo.sendData(Channel.BUNGEECORD, out.toByteArray())
    }

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        val listener = listeners[subChannel]
        if (listener == null) {
            logger.warning("No listener for $subChannel subchannel. Message will be discarded")
            return
        }

        logger.verbose("Bungeecord received message: $subChannel")

        listener.onReceiveMessage(event.receiver, event.sender, stream)
    }
}