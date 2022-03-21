package com.projectcitybuild.modules.channels.spigot

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

class SpigotMessageListener(
    private val logger: PlatformLogger
) : PluginMessageListener {

    private val listeners = HashMap<String, SpigotSubChannelListener>()

    fun register(listener: SpigotSubChannelListener) {
        listeners[listener.subChannel] = listener
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

        logger.verbose("Spigot received message: $subChannel")

        listener.onSpigotReceivedMessage(player, stream)
    }
}
