package com.projectcitybuild.modules.channels.bungeecord

import com.google.common.io.ByteArrayDataInput
import net.md_5.bungee.api.connection.Connection

interface BungeecordSubChannelListener {

    val subChannel: String

    fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput)
}