package com.projectcitybuild.modules.channels.bungeecord

import com.google.common.io.ByteStreams
import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.logger.PlatformLogger
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.event.EventHandler

class BungeecordMessageListener(
    private val logger: PlatformLogger
): BungeecordListener {

    private val listeners = HashMap<String, BungeecordSubChannelListener>()

    fun register(listener: BungeecordSubChannelListener) {
        listeners[listener.subChannel] = listener
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

        listener.onBungeecordReceivedMessage(event.receiver, event.sender, stream)
    }
}