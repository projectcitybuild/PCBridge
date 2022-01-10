package com.projectcitybuild.platforms.bungeecord

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import net.md_5.bungee.api.config.ServerInfo

class MessageToSpigot(
    private val serverInfo: ServerInfo,
    private val subChannel: String,
    private val params: Array<out Any> = emptyArray()
) {
    fun send() {
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

        serverInfo.sendData(Channel.BUNGEECORD, out.toByteArray())
    }
}