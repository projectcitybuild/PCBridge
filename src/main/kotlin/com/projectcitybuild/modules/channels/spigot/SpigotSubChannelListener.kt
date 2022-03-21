package com.projectcitybuild.modules.channels.spigot

import com.google.common.io.ByteArrayDataInput
import org.bukkit.entity.Player

interface SpigotSubChannelListener {

    val subChannel: String
    fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput)
}
