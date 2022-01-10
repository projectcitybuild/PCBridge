package com.projectcitybuild.modules.channels

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.logger.LoggerProvider
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class SpigotMessageListener(
    private val logger: LoggerProvider
): PluginMessageListener {

    private val listeners = HashMap<String, SubChannelListener>()

    fun register(subChannel: String, listener: SubChannelListener) {
        listeners[subChannel] = listener
    }

    override fun onPluginMessageReceived(channel: String?, player: Player?, message: ByteArray?) {
        if (channel != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(message)
        val subChannel = stream.readUTF()

        val listener = listeners[subChannel]
        if (listener == null) {
            logger.warning("No listener for $subChannel subchannel. Message will be discarded")
            return
        }

        listener.onSpigotMessageReceived(player, stream)
    }
}