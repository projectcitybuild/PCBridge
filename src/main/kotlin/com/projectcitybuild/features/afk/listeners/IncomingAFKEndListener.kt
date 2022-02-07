package com.projectcitybuild.features.afk.listeners

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import net.md_5.bungee.api.ProxyServer
import org.bukkit.entity.Player

class IncomingAFKEndListener(
    private val proxy: ProxyServer,
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