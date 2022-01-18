package com.projectcitybuild.features.warps.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.warps.repositories.WarpRepository
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.textcomponentbuilder.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.time.LocalDateTime
import javax.inject.Inject

class IncomingSetWarpListener @Inject constructor(
    private val warpRepository: WarpRepository
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

        if (warpRepository.exists(warpName)) {
            receiver.send().error("A warp for $warpName already exists")
            return
        }

        val warp = Warp(
            warpName,
            CrossServerLocation(
                serverName,
                worldName,
                x,
                y,
                z,
                pitch,
                yaw,
            ),
            LocalDateTime.now()
        )
        CoroutineScope(Dispatchers.IO).launch {
            warpRepository.add(warp)
            receiver.send().success("Created warp for $warpName")
        }
    }
}