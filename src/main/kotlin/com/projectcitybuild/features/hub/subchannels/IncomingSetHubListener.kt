package com.projectcitybuild.features.hub.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.CrossServerLocation
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.entities.Warp
import com.projectcitybuild.features.hub.repositories.HubRepository
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.time.LocalDateTime
import javax.inject.Inject

class IncomingSetHubListener @Inject constructor(
    private val hubRepository: HubRepository,
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

        val warp = Warp(
            "hub",
            CrossServerLocation(
                serverName,
                worldName,
                x,
                y,
                z,
                pitch,
                yaw,
            ),
            LocalDateTime.now(),
        )
        hubRepository.save(warp, receiver.uniqueId)

        receiver.send().success("Destination of /hub has been set")
    }
}