package com.projectcitybuild.platforms.bungeecord.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.storage.HubFileStorage
import com.projectcitybuild.modules.storage.SerializableDate
import com.projectcitybuild.modules.storage.SerializableUUID
import com.projectcitybuild.platforms.bungeecord.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import java.util.*

class IncomingSetHubListener(
    private val hubStorage: HubFileStorage
): Listener {

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        if (subChannel != SubChannel.SET_HUB)
            return

        if (event.receiver !is ProxiedPlayer)
            return

        val player = event.receiver as ProxiedPlayer
        val serverName = player.server.info.name

        val worldName = stream.readUTF()
        val x = stream.readDouble()
        val y = stream.readDouble()
        val z = stream.readDouble()
        val pitch = stream.readFloat()
        val yaw = stream.readFloat()

        val warp = Warp(
            serverName,
            worldName,
            SerializableUUID(player.uniqueId),
            x,
            y,
            z,
            pitch,
            yaw,
            SerializableDate(Date())
        )
        CoroutineScope(Dispatchers.IO).launch {
            hubStorage.save(warp)
            player.send().success("Destination of /hub has been set")
        }
    }
}