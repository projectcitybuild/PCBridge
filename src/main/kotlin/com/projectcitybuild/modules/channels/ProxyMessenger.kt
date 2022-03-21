package com.projectcitybuild.modules.channels

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import javax.inject.Inject

class ProxyMessenger @Inject constructor(
    private val plugin: Plugin,
) {
    fun sendToProxy(sender: Player, subChannel: String, params: Array<out Any> = emptyArray()) {
        val out = ByteStreams.newDataOutput()
        out.writeUTF(subChannel)

        for (param in params) {
            when (param) {
                is String -> out.writeUTF(param)
                is Int -> out.writeInt(param)
                is Double -> out.writeDouble(param)
                is Float -> out.writeFloat(param)
                is Boolean -> out.writeBoolean(param)
                is Short -> out.writeShort(param.toInt())
                is Long -> out.writeLong(param)
                is Byte -> out.writeByte(param.toInt())
                is Char -> out.writeChar(param.code)
            }
        }
        sender.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())
    }
}