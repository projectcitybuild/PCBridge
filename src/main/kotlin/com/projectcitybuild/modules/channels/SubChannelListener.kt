package com.projectcitybuild.modules.channels

import com.google.common.io.ByteArrayDataInput
import org.bukkit.entity.Player

interface SubChannelListener {

    fun onSpigotMessageReceived(player: Player?, stream: ByteArrayDataInput)
}