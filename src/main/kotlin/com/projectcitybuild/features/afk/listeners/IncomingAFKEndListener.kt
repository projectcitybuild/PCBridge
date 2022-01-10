package com.projectcitybuild.features.afk.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.sessioncache.SessionCache
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class IncomingAFKEndListener(
    private val proxy: ProxyServer,
    private val sessionCache: SessionCache
): Listener {

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        if (subChannel != SubChannel.AFK_END)
            return

        val player = event.receiver as? ProxiedPlayer ?: return

        if (sessionCache.afkPlayerList.contains(player.uniqueId)) {
            sessionCache.afkPlayerList.remove(player.uniqueId)

            val shouldBroadcastToPlayers = stream.readBoolean()
            println(shouldBroadcastToPlayers)
            if (shouldBroadcastToPlayers) {
                proxy.broadcast(
                    TextComponent("${player.displayName} is no longer AFK").also {
                        it.color = ChatColor.GRAY
                        it.isItalic = true
                    }
                )
            }
        }
    }
}