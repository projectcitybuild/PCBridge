package com.projectcitybuild.features.playercache.listeners

import com.projectcitybuild.core.BungeecordListener
import com.projectcitybuild.features.joinmessage.events.FirstTimeJoinEvent
import com.projectcitybuild.modules.playerconfig.PlayerConfigCache
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.time.LocalDateTime
import javax.inject.Inject

class PlayerCacheListener @Inject constructor(
    private val plugin: Plugin,
    private val playerCache: PlayerConfigCache,
    private val playerConfigRepository: PlayerConfigRepository,
): BungeecordListener {

    @EventHandler
    fun onPostLoginEvent(event: PostLoginEvent) {
        val uuid = event.player.uniqueId

        if (playerConfigRepository.get(uuid) == null) {
            plugin.proxy.pluginManager.callEvent(
                FirstTimeJoinEvent(event.player)
            )
            playerConfigRepository.add(
                uuid = uuid,
                isMuted = false,
                isAllowingTPs = true,
                firstSeen = LocalDateTime.now(),
            )
        }
    }

    @EventHandler
    fun onPlayerDisconnectEvent(event: PlayerDisconnectEvent) {
        playerCache.remove(event.player.uniqueId)
    }
}