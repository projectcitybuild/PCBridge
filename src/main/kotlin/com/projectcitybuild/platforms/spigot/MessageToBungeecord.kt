package com.projectcitybuild.platforms.spigot

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class MessageToBungeecord(
    private val plugin: Plugin,
    private val sender: Player,
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
//        plugin.server.scheduler.runTaskAsynchronously(plugin) {
            sender.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())
//        }
    }
}