package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.IOException

class MessageToBungeecord(
    private val plugin: Plugin,
    private val sender: Player,
    private val subChannel: String,
    vararg params: Any
) {
    private val params: Array<out Any>

    init {
        this.params = params
    }

    fun send() {
        try {
            ByteArrayOutputStream().use { b ->
                DataOutputStream(b).use { out ->
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
                    plugin.server.scheduler.runTaskAsynchronously(plugin) {
                        sender.sendPluginMessage(plugin, Channel.BUNGEECORD, b.toByteArray())
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}