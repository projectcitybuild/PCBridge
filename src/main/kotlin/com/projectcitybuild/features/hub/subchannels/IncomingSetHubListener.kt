package com.projectcitybuild.features.hub.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.LegacyWarp
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.old_modules.storage.HubFileStorage
import com.projectcitybuild.entities.serializables.SerializableDate
import com.projectcitybuild.entities.serializables.SerializableUUID
import com.projectcitybuild.modules.textcomponentbuilder.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class IncomingSetHubListener(
    private val hubStorage: HubFileStorage
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.SET_HUB

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        if (receiver !is ProxiedPlayer)
            return

        val serverName = receiver.server.info.name

        val worldName = stream.readUTF()
        val x = stream.readDouble()
        val y = stream.readDouble()
        val z = stream.readDouble()
        val pitch = stream.readFloat()
        val yaw = stream.readFloat()

        val warp = LegacyWarp(
            serverName,
            worldName,
            SerializableUUID(receiver.uniqueId),
            x,
            y,
            z,
            pitch,
            yaw,
            SerializableDate(Date())
        )
        CoroutineScope(Dispatchers.IO).launch {
            hubStorage.save(warp)
            receiver.send().success("Destination of /hub has been set")
        }
    }
}