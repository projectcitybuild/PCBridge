package com.projectcitybuild.features.warps.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.old_modules.storage.serializers.SerializableDate
import com.projectcitybuild.old_modules.storage.serializers.SerializableUUID
import com.projectcitybuild.old_modules.storage.WarpFileStorage
import com.projectcitybuild.modules.textcomponentbuilder.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

class IncomingSetWarpListener(
    private val warpStorage: WarpFileStorage
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.SET_WARP

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        if (receiver !is ProxiedPlayer)
            return

        val serverName = receiver.server.info.name

        val warpName = stream.readUTF()
        val worldName = stream.readUTF()
        val x = stream.readDouble()
        val y = stream.readDouble()
        val z = stream.readDouble()
        val pitch = stream.readFloat()
        val yaw = stream.readFloat()

        if (warpStorage.exists(warpName)) {
            receiver.send().error("A warp for $warpName already exists")
            return
        }

        val warp = Warp(
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
            warpStorage.save(warpName, warp)
            receiver.send().success("Created warp for $warpName")
        }
    }
}