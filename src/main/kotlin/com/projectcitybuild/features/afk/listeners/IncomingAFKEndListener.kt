package com.projectcitybuild.features.afk.listeners

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.sessioncache.SpigotSessionCache
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.event.EventHandler
import org.bukkit.entity.Player

class IncomingAFKEndListener(
    private val proxy: ProxyServer,
    private val spigotSessionCache: SpigotSessionCache
): SpigotSubChannelListener {

    override val subChannel = SubChannel.AFK_END

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
//        if (spigotSessionCache.afkPlayerList.contains(player.uniqueId)) {
//            spigotSessionCache.afkPlayerList.remove(player.uniqueId)
//
//            val shouldBroadcastToPlayers = stream.readBoolean()
//            println(shouldBroadcastToPlayers)
//            if (shouldBroadcastToPlayers) {
//                proxy.broadcast(
//                    TextComponent("${player.displayName} is no longer AFK").also {
//                        it.color = ChatColor.GRAY
//                        it.isItalic = true
//                    }
//                )
//            }
//        }
    }
}